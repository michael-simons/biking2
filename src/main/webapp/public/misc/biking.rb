# encoding: UTF-8
# $Id$
#
# Created by Michael Simons, michael-simons.eu
# and released under The BSD License
# http://www.opensource.org/licenses/bsd-license.php
#
# Copyright (c) 2009, Michael Simons
# All rights reserved.
#
# Redistribution  and  use  in  source   and  binary  forms,  with  or   without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source   code must retain   the above copyright   notice,
#   this list of conditions and the following disclaimer.
#
# * Redistributions in binary  form must reproduce  the above copyright  notice,
#   this list of conditions  and the following  disclaimer in the  documentation
#   and/or other materials provided with the distribution.
#
# * Neither the name  of  michael-simons.eu   nor the names  of its contributors
#   may be used  to endorse   or promote  products derived  from  this  software
#   without specific prior written permission.
#
# THIS SOFTWARE IS  PROVIDED BY THE  COPYRIGHT HOLDERS AND  CONTRIBUTORS "AS IS"
# AND ANY  EXPRESS OR  IMPLIED WARRANTIES,  INCLUDING, BUT  NOT LIMITED  TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL  THE COPYRIGHT HOLDER OR CONTRIBUTORS  BE LIABLE
# FOR ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL,  EXEMPLARY, OR  CONSEQUENTIAL
# DAMAGES (INCLUDING,  BUT NOT  LIMITED TO,  PROCUREMENT OF  SUBSTITUTE GOODS OR
# SERVICES; LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT  LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE  USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
%w(rubygems bundler).each{|lib| require lib}
Bundler.require

require "sinatra/reloader" if development?

# -----------------------------
# Configuration
# -----------------------------
# Applies to all environments
configure do
  disable :protection
  
  # Home is…
  @@home = {:latitude => 50.75144902272457, :longitude => 6.179489185520004}
  
  # Create a global memcache client instance...
  @@cache = MemCache.new({
    :c_threshold => 10000,
    :compression => true,
    :debug => false,
    :namespace => 'biking.michael-simons.eu',
    :readonly => false,
    :urlencode => false
  })
  @@cache.servers = '127.0.0.1:11211'

  # Be aware that if running under Passenger memcache connections are forked which is harmful
  # as described here:
  # http://modrails.com/documentation/Users%20guide%20Apache.html#_example_1_memcached_connection_sharing_harmful
  if defined?(PhusionPassenger)
    PhusionPassenger.on_event(:starting_worker_process) do |forked|
      @@cache.reset if forked
    end
  end

  # A global alias for the URI module so it can be used in the models
  @@uri = URI
  
  mime_type :json, 'application/json'
end

# Environment specific configuration
configure :development do  
  GOOGLE_MAPS_KEY = 'ABQIAAAAQYquIKJoTAELBUHmN6N5UhTJQa0g3IQ9GZqIMmInSLzwtGDKaBToqDeFonF489Jbb_wtRzqHzDSeHQ'
  DataMapper.setup(:default, "sqlite3:///#{Dir.pwd}/biking.dev.sqlite3")  
end
configure :production  do
  GOOGLE_MAPS_KEY = 'ABQIAAAAvDdVB0EqLdDhl7gZSrofjRQrBFQsTQPLCYY8thQUPjcX2PGOgRRFOifTxicA7ztL0Ifaw162MDwhAA'
  DataMapper.setup(:default, "sqlite3:///#{Dir.pwd}/biking.prod.sqlite3")
end

# -----------------------------
# Auth
# -----------------------------
# A helper module that can be included via require_administrative_privileges
# in routes that needs auth. The module is made available through the helpers
# declartion
module Authorization
  def auth
    @auth ||= Rack::Auth::Basic::Request.new(request.env)
  end

  def unauthorized!(realm="biking.michael-simons.eu")
    headers 'WWW-Authenticate' => %(Basic realm="#{realm}")
    throw :halt, [ 401, 'Authorization Required' ]
  end

  def bad_request!
    throw :halt, [ 400, 'Bad Request' ]
  end

  def authorized?
    request.env['REMOTE_USER']
  end

  def authorize(login, password)
    # Authorize through a user login and password.
    # Yeah, i know, password is stored in clean text
    User.first(:login => login, :password => password) != nil
  end

  def require_administrative_privileges
    return if authorized?
    unauthorized! unless auth.provided?
    bad_request! unless auth.basic?
    unauthorized! unless authorize(*auth.credentials)
    request.env['REMOTE_USER'] = auth.username
  end
end

helpers do
  include Authorization
  def base_url
    @base_url ||= "#{request.env['rack.url_scheme']}://#{request.env['HTTP_HOST']}"
  end
end

# -----------------------------
# Model
# -----------------------------
# Just needed to authenticate users
class User
  include DataMapper::Resource

  property :id,          Serial
  property :login,       String, :length => 16, :required => true, :unique => true
  property :password,    String, :length => 255, :required => true, :unique => true
end

# A bike stores its name and a color for the google charts and has many milages.
class Bike
  include DataMapper::Resource
  property :id,          Serial
  property :name,        String, :length => 255, :required => true, :unique => true
  property :color,       String, :length => 6, :required => true, :default => 'CCCCCC'
  property :created_at,  DateTime
  property :decommissioned, Date
  property :bought_on,   Date
  validates_uniqueness_of :name

  # Assoziations...
  has n, :milages, :order => [:when.asc]
  # Datamapper doesn't destroy depended rows
  before :destroy, :destroy_milages!

  # A milage report is defined as periods from one milage to the next.
  # The distance travelled in this period is the
  # difference between the new milage and the previous
  def milage_report
    s = milages.size
    @milage_report ||=
      unless s >= 1 then Array.new
      else milages[1,s].collect{|m| {:period => m.left_sibling.when, :milage => (m.value - m.left_sibling.value).to_f}} end
  end

  def to_s
    name + (milages.count > 0 ? " (#{milages.last.value.to_i}km)" : '')
  end

  # Calculates the sum of this bikes milages
  def sum
    @sum ||= milage_report.inject(0){|sum, m| sum + m[:milage]}
  end

  def destroy_milages!
    milages.all.destroy!
  end
  
  def milage_in_period(period)
    (rv = milage_report.find{|m| m[:period] == period}) == nil ? 0 : rv[:milage]
  end
end

# A milage is stored for a bike at the beginning of the month.
# Its the milage of the bike at this point of time.
class Milage
  include DataMapper::Resource
  property :id,          Serial
  property :when,        Date, :required => true
  property :value,       Decimal, :required => true, :precision => 8, :scale => 2
  property :created_at,  DateTime

  belongs_to :bike

  validates_uniqueness_of :when, :scope => :bike

  is :list, :scope => [:bike_id]
end

# Just a trip that i took on some other bikes
class AssortedTrip
  include DataMapper::Resource
  property :id,          Serial
  property :when,        Date, :required => true
  property :distance,    Decimal, :required => true, :precision => 8, :scale => 2
end

# A point it space...
class Location
  include DataMapper::Resource
  property :id,           Serial
  property :created_at,   DateTime
  property :latitude,     Decimal, :required => true, :precision => 18, :scale => 15
  property :longitude,    Decimal, :required => true, :precision => 18, :scale => 15
  property :description,  String, :length => 2048

  def Location.convert_decimal_to_angle(val)
    degrees = val.to_i
    minutes = (val - degrees) * 60
    seconds = (minutes - minutes.to_i) * 60

    return degrees, minutes.to_i, seconds
  end

  def to_s
    la_d, la_m, la_s = Location.convert_decimal_to_angle latitude
    lo_d, lo_m, lo_s = Location.convert_decimal_to_angle longitude

    return "#{description} (#{la_d}°#{la_m}'#{sprintf('%2.3f', la_s)}'', #{lo_d}°#{lo_m}'#{sprintf('%2.3f', lo_s)}'')"
  end
end

# Biking pictures are taken from http://dailyfratze.de/michael.
# Its all pictures tagged with Theme/Radtour.
# The biking up just stores the image urls and the links, not
# the images itself
class BikingPicture
  include DataMapper::Resource
  property :id,   Serial
  property :url,  String,  :length => 512
  property :link, String,  :length => 512
  validates_uniqueness_of :url

  # Gets a random biking picture
  def BikingPicture.random_url     
    # Check if pictures are cached...
    biking_pictures = @@cache['biking_pictures']
    unless biking_pictures then
      # Start retrieving the feed
      url = @@uri.parse("http://dailyfratze.de/michael/tags/Thema/Radtour?format=rss&dir=d")
      next_page = false

      Net::HTTP.new(url.host).start do |http|
        # Its a media rss feed that defines previous and next feeds
        while url
          req = Net::HTTP::Get.new("#{url.path}?#{url.query}")
          xml = http.request(req).body
          # Parse the data
          doc = LibXML::XML::Parser.io(StringIO.new(xml)).parse
          # Unless this url hasn't been retrieved...          
          unless @@cache[url.to_s]            
            doc.find('/rss/channel/item').each do |item|
              # Get all pictures and store them if not already grapped
              biking_picture = BikingPicture.first :url => item['url']
              unless biking_picture
                biking_picture = BikingPicture.new({:url => item.find_first('media:thumbnail')['url'], :link => item.find_first('link').content })
                biking_picture.save
              end
            end
            # Mark this url as seen
            @@cache[url.to_s] = next_page
            next_page = true                          
          end
          # Check if there are more feeds...
          next_url = doc.find_first("/rss/channel/atom:link[@rel = 'next']")
          url = if next_url
            @@uri.parse next_url['href']
          else
            nil
          end
        end
      end
      # Get the data...
      biking_pictures = BikingPicture.all()
      # ...and store it
      @@cache.set 'biking_pictures', biking_pictures, 3600
    end

    biking_pictures.sort_by{rand}[0]
  end
end

class Track
  include DataMapper::Resource
  
  TYPES = ['biking', 'running']
  
  property :id,        Serial
  property :name,      String,  :required => true, :length => 512
  property :when,      Date, :required => true
  property :description, Text
  property :minlat,    Decimal, :precision => 18, :scale => 15
  property :minlon,    Decimal, :precision => 18, :scale => 15
  property :maxlat,    Decimal, :precision => 18, :scale => 15
  property :maxlon,    Decimal, :precision => 18, :scale => 15
  property :type,      String, :required => true, :default => 'biking'
  validates_uniqueness_of :name, :scope => :when
  
  def self.by_pretty_id(pretty_id)
    Track.get pretty_id.to_i(36)
  end
  
  def pretty_id
    id.to_s(36)
  end
end
  
DataMapper.finalize  
DataMapper.auto_upgrade!

# -----------------------------
# Controller
# -----------------------------

#
# List all bikes and other trips
#
get '/' do  
  @bikes = Bike.all :decommissioned => nil, :order => [:name.asc]
  @decommissioned_bikes = Bike.all :decommissioned.not => nil, :order => [:name.asc]
  @trips = AssortedTrip.all :order => [:when.asc]

  @sum_bikes = (@bikes + @decommissioned_bikes).inject(0) {|sum, b| sum + b.sum}
  @sum_trips = @trips.inject(0) {|sum, b| sum + b.distance}.to_f

  @min = Milage.min(:when, Milage.bike.decommissioned => nil) || Time.new
  @max = Milage.max(:when, Milage.bike.decommissioned => nil) || @min
  
  @min_decommissioned = Milage.min(:when, Milage.bike.decommissioned.not => nil) || Time.new
  @max_decommissioned = Milage.max(:when, Milage.bike.decommissioned.not => nil) || @min_decommissioned

  @a_biking_picture = BikingPicture.random_url

  @on_index = true
  
  haml :index
end

get '/biking_picture' do
  BikingPicture.random_url.to_json
end

#
# Displays the about page
#
get '/about' do
  @active_page = 'about'
  @title_path = 'About'
  haml :about
end

#
# Displays the new_bike form
#
get '/new_bike' do
  require_administrative_privileges
  @title_path = 'Create a new bike'
  haml :new_bike
end

#
# Creates a new bike named name
# i.e.  curl -d name=Speed http://localhost:4567/bikes
#
post '/bikes' do
  require_administrative_privileges
  bike = Bike.new params
  bike.save
  redirect '/'
end

#
# Deletes the bike named name
# i.e. curl -X DELETE http://localhost:4567/bike/Speed
#
delete '/bike/:name' do |name|
  require_administrative_privileges
  bike = Bike.first :name => name
  bike.destroy if bike
  redirect '/'
end

#
# Displays the new_milage for a bike form
#
get '/bike/:name/new_milage' do |name|
  require_administrative_privileges
  @bike = Bike.first :name => name
  if @bike then 
    @title_path = "Create a new milage for \"#{@bike.name}\""
    haml :new_milage
  else redirect '/' end
end

#
# Add a new milage to the bike named :name
# i.e. curl http://localhost:4567/bike/Speed/milages -d milage[value]=2000 -d milage[when]=2000-01-01
#
post '/bike/:name/milages' do |name|
  require_administrative_privileges
  bike = Bike.first :name => name
  if bike then
    bike.milages << Milage.new(params[:milage]) if bike
    bike.save
  end
  redirect '/'
end

delete '/bike/:name/milage/:id' do |name,id|
  require_administrative_privileges
  bike = Bike.first :name => name
  bike.milages.get(id).destroy if bike
  redirect '/'
end

#
# Displays the new_trip form
#
get '/new_trip' do
  require_administrative_privileges
  @title_path = 'Create a new trip'
  haml :new_trip
end

#
# Adds a new trip
#
post '/trips' do
  require_administrative_privileges
  trip = AssortedTrip.new params
  trip.save
  redirect '/'
end

get '/new_track' do
  require_administrative_privileges
  @title_path = 'Upload a new track'
  haml :new_track
end

#
# Upload a new track
#
post '/tracks' do
  require_administrative_privileges
  track = Track.new({:name => params[:name], :when => params[:when], :type => params[:type]})
  track.description = params[:description] unless params[:description].blank?
  if !params[:data].blank? && track.save    
    filename = File.join("tracks", track.id.to_s)
    datafile = params[:data]
    File.open("#{filename}.tcx", 'wb') { |file| file.write(datafile[:tempfile].read)}
    gpsbabel = "`which gpsbabel`"
    system("#{gpsbabel} -i gtrnctr -f #{filename}.tcx -o gpx -F #{filename}.gpx")
    doc = LibXML::XML::Parser.file("#{filename}.gpx").parse
    ns = 'ns:http://www.topografix.com/GPX/1/0'
    bounds = doc.find_first('/ns:gpx/ns:bounds', ns)
    track.minlat = bounds['minlat']
    track.minlon = bounds['minlon']
    track.maxlat = bounds['maxlat']
    track.maxlon = bounds['maxlon']
    track.save
    redirect "/tracks/#{track.pretty_id}"
  else
    redirect '/'
  end
end

#
# Displays a list of tracks
#
get '/tracks' do
  @active_page = 'tracks'
  @tracks = Track.all(:order => [:when.asc])
  @title_path = 'Tracks'
  haml :tracks
end

#
# Oembed a track
#
get '/oembed' do
  id = nil
  if m = /.*?\/tracks\/(\w+)(\/|\.(\w+))?$/.match(params['url']) then
    id = m[1]
  end  
  track = Track.by_pretty_id(id) unless id.blank?
  
  raise Sinatra::NotFound if track.blank?
  
  width, height = params[:maxwidth].to_i, params[:maxheight].to_i
  width = 1024 if width == 0
  height = 576 if height == 0
  
  format = (params['format'] || 'json').downcase
  
  rv = {
    :type => 'rich',
    :version => '1.0',
    :title => track.name,
    :author_name => 'Michael J. Simons',
    :author_url => 'http://michael-simons.eu',
    :provider_name => 'biking.rb',
    :provider_url => 'http://biking.michael-simons.eu',
    :cache_age => 24 * 60 * 60,
    :html => "<iframe width='#{width}' height='#{height}' src='#{base_url()}/tracks/#{id}/embed?width=#{width}&height=#{height}' class='bikingTrack'></iframe>"
  }
  
  case format
  when 'json'
    content_type :json
    rv.to_json
  when 'xml'
    status 406
  else
    raise Sinatra::NotFound
  end
end

#
# Displays a track
#
get %r{/tracks/(\w+)(/|\.(\w+))?$} do
  id = params[:captures][0]
  format = if params[:captures][2].blank?
    'html'
  else
    params[:captures][2]
  end.downcase
  
  @track = Track.by_pretty_id(id)
  raise Sinatra::NotFound if @track.blank? or !%w(html tcx gpx).include?(format)
  
  if format == 'html'
    @home = Location.new(:latitude => @@home[:latitude], :longitude => @@home[:longitude], :description => 'Home')
    haml :map
  else
    send_file(File.join("tracks", "#{@track.id}.#{format}"), :disposition => 'attachment', :filename => "#{@track.name}.#{format}", :type => 'text/xml')
  end
end

#
# Embed a track
#
get %r{/tracks/(\w+)/embed} do
	id = params[:captures][0]
	@track = Track.by_pretty_id(id)
  raise Sinatra::NotFound if @track.blank?
  @width, @height = params[:width].to_i, params[:height].to_i
  @width = 1024 if @width == 0
  @height = 576 if @height == 0
  @home = Location.new(:latitude => @@home[:latitude], :longitude => @@home[:longitude], :description => 'Home')
  haml :embedded_track, :layout => false
end

#
# Some additional stylesheet information along with the w3c core style
#
get '/stylesheet.css' do
  headers 'Content-Type' => 'text/css; charset=utf-8'
  sass :stylesheet
end

__END__

# -----------------------------
# View
# -----------------------------

@@ stylesheet
body
  :padding-top 40px
.biking_picture img
  :display block
  :margin-left auto
  :margin-right auto
.anchor:before
  :display block
  :content ''
  :height 40px
  :margin -40px 0 0
.left
  :text-align left
.right
  :text-align right
  :padding-right 5px
.row0, .row0 td
  :background-color #ded7c9
tbody th.sum
  :border-left 1px solid #000
tfoot th.sum
  :border-top 1px solid #000
table.trip_table
  :width 100%
  :border-collapse collapse
  :border 1px solid #ccc
table.track_table
  :width 100%
th.period, td.period
  :width 85px
table.trip_table td, table.track_table td
  :vertical-align top
h2 small
  :font-size 50% !important
#google
  :font-size 10px !important
  :line-height normal !important
  a
    :background none !important
table.track_table
  a
    :background none !important

@@ layout
-#-----------------------------
!!! 5
%html
  %head
    %script{:src => '/js/modernizr.min.js', :type => 'text/javascript'}
    %meta{'http-equiv' => 'Content-Type', :content => 'text/html; charset=utf-8'}
    %meta{'name'       => 'Author',       :content => 'Michael J. Simons'}
    %meta{'name'       => 'viewport',     :content => 'width=device-width, initial-scale=1.0' }
    %title= "biking.michael-simons.eu#{' > ' + @title_path if @title_path}"
    %link{:rel => 'stylesheet', :href => '/css/bootstrap.min.css', :type => 'text/css'}
    %link{:rel => 'stylesheet', :href => '/css/bootstrap-responsive.css', :type => 'text/css'}
    %link{:rel => 'stylesheet', :href => '/css/bootstrap-datepicker.css', :type => 'text/css'}    
    %link{:rel => 'stylesheet', :href => '/stylesheet.css', :type => 'text/css'}
    %link{:rel => 'stylesheet', :href => '/css/openlayersstyle.css', :type => 'text/css'}
    %link{:rel => 'stylesheet', :href => '/css/openlayersgoogle.css', :type => 'text/css'}
    %link{:rel => 'icon', :href => '/images/favicon.ico', :type => 'image/x-icon'}
    %script{:src => '/js/jquery.min.js', :type => 'text/javascript'}
    %script{:src => '/js/bootstrap.min.js', :type => 'text/javascript'}
    %script{:src => '/js/bootstrap-datepicker.min.js', :type => 'text/javascript'}    
    %script{:src => '/js/moment.min.js', :type => 'text/javascript'}    
    %script{:src => '/js/highcharts.js', :type => 'text/javascript'}        
    %script{:src => "http://maps.google.com/maps?file=api&v=2&key=#{GOOGLE_MAPS_KEY}", :type => 'text/javascript'}
    %script{:src => 'http://openlayers.org/api/2.11/OpenLayers.js', :type => 'text/javascript'}
    %script{:src => 'http://www.openstreetmap.org/openlayers/OpenStreetMap.js', :type => 'text/javascript'}    
  %body{:data => {'spy' => 'scroll', 'target' => '.navbar'}}
    #err.warning= env['sinatra.error']
    .navbar.navbar-inverse.navbar-fixed-top
      .navbar-inner
        .container
          %button{:class => 'btn btn-navbar', :data => {'toggle' => 'collapse',  'target' => '.nav-collapse'}}
            %span{:class => 'icon-bar'}
            %span{:class => 'icon-bar'}
            %span{:class => 'icon-bar'}
          %a{:href => '/', :class => 'brand'} Michis milage
          .nav-collapse.collapse
            %ul.nav
              %li
                %a{:href => "#{'/' unless defined? @on_index}#current"} Current year
              %li
                %a{:href => "#{'/' unless defined? @on_index}#history"} History
              %li
                %a{:href => "#{'/' unless defined? @on_index}#details"} Details
              %li{:class => "#{'active' if 'tracks' == @active_page}"}
                %a{:href => '/tracks'} Tracks                
              %li{:class => "#{'active' if 'about' == @active_page}"}
                %a{:href => '/about'} About
    .container
      = yield
      
      %hr
      %footer
        %p
          &#160; &copy; 2009-#{Date.today.year}
          %a{:href => 'http://michael-simons.eu'} Michael Simons          
          
    :javascript
      // Fix date input fiels
      if(!Modernizr.inputtypes['date'])
        $('input[type="date"]').datepicker({'format': 'yyyy-mm-dd', 'autoclose': true});
        
      // Create charts
      var chartTables = $('table.trip_table.use-in-charts');
      if(chartTables.length > 0) {
        var data = {};
        chartTables.each(function() {
          var names = new Array();
          $('thead > tr > th:not(:first-child):not(:last-child)', this).each(function() {
            names.push($(this).text());
          });

          $('tbody > tr', this).each(function() {        
            var period = 'p' + $('td:first-child', this).text().replace(/\-/,'');
            createNestedObject(data, [period]);
            $('td:not(:first-child):not(:last-child)', $(this)).each(function() {
              createNestedObject(data, [period, names[$(this).index()-1]], parseFloat($(this).text()));            
            });   
          });
        });

        var index = new Array();
        for(var period in data)
          index.push(period);
        index.sort();

        var minYear = moment(index[0], '[p]YYYYMM').year();
        var maxYear = moment(index[index.length-1], '[p]YYYYMM').year();

        var createCurrentDataChart = function(data, index, maxYear) {
          var allBikes = {};
          var categories = new Array();
          for(var i=0; i<12;++i) {
            m = moment([maxYear,i,1]);
            categories.push(m.format('MMM'));
            for(var bike in data[m.format('[p]YYYYMM')]) {  
              if(!allBikes.hasOwnProperty(bike)) {
                allBikes[bike] = true;
              }
            }
          }

          var series = new Array();
          var maxY = 0;
          var sum = {name: 'Sum', data: [0,0,0,0,0,0,0,0,0,0,0,0], type: 'spline'};
          for(var bike in allBikes) {
            var serie = {name: bike, data: new Array(), type: 'column'}
            for(var i=0; i<12; ++i) {
              period = moment([maxYear,i,1]).format('[p]YYYYMM');
              serie.data[i] = data.hasOwnProperty(period) && data[period].hasOwnProperty(bike) ? data[period][bike] : 0;
              sum.data[i] += serie.data[i];
              if(serie.data[i] > maxY)
                maxY = serie.data[i];
            }
            series.push(serie);
          }
          series.push(sum);

          $('#current-data')
            .css('max-width','640px')
            .css('height', '300px')
            .css('display','block')
            .highcharts({
              chart: {borderWidth: '1'},
              credits: {enabled: false},
              title: {text: "Michis milage in " + maxYear},
              xAxis: {categories: categories},
              yAxis: {
                min: 0,
                max: maxY,
                tickInterval: 100,
                endOnTick: true,
                title: {text: 'Milage (km)'}
              },
              tooltip: {
                headerFormat: '{point.key}<table>',
                pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td><td style="padding:0"><b>{point.y:.1f} km</b></td></tr>',
                footerFormat: '</table>',
                shared: true,
                useHTML: true
              },
              plotOptions: {
                column: {
                  pointPadding: 0.2,
                  borderWidth: 0
                },
                series: {
                  animation: false
                }
              },
              series: series
            });
        };
        
        var createHistoricalDataChart = function(data, index, minYear, maxYear) {
          var categories = new Array();
          for(var i=0; i<12;++i) 
            categories.push(moment([maxYear,i,1]).format('MMM'));
          
          var hlp = {};
          for (var i = 0; i < index.length; i++) {
            var m = moment(index[i], '[p]YYYYMM');
            year = m.year().toString();
            month = m.month();
            if(year == maxYear)
              continue;
            series = null;
            if(!hlp.hasOwnProperty(year))
              hlp[year] = {name: year, data: [0,0,0,0,0,0,0,0,0,0,0,0]}
            series = hlp[year];
            for(var bike in data[index[i]])
              series.data[month] += data[index[i]][bike];
          }

          series = new Array();
          for(var year in hlp)
            series.push(hlp[year]);

          $('#historical-data')
            .css('max-width','640px')
            .css('height', '380px')
            .css('display','block')
            .highcharts({
              chart: {borderWidth: '1', type:'line'},
              credits: {enabled: false},
              title: {text: "Milage " + minYear + '-' + (maxYear-1)},
              xAxis: {categories: categories},
              yAxis: {
                min: 0,
                tickInterval: 100,
                endOnTick: true,
                title: {
                  text: 'Milage (km)'
                }
              },
              tooltip: {
                headerFormat: '{point.key}<table>',
                pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td><td style="padding:0"><b>{point.y:.1f} km</b></td></tr>',
                footerFormat: '</table>',
                shared: true,
                useHTML: true
              },
              plotOptions: {
                column: {
                  pointPadding: 0.2,
                  borderWidth: 0
                },
                series: {animation: false}
              },
              series: series
            });
        }
        createCurrentDataChart(data, index, maxYear);
        createHistoricalDataChart(data, index, minYear, maxYear);
      }
        
      // Google analytics code
      var _gaq = _gaq || [];
      _gaq.push(['_setAccount', 'UA-4558057-6']);
      _gaq.push(['_trackPageview']);
      (function() {
        var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
        var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
      })();

@@ bike_table
-#-----------------------------
%table{:class => "trip_table #{classes}"}
  %thead
    %tr
      %th{:class => "left period"} Period
      - for bike in the_bikes
        %th{:class =>"right"}= bike.name
      %th{:class => "right"}
  %tfoot
    %tr
      %th
      - for bike in the_bikes
        %th{:class =>"right sum"}= bike.sum
      %th
    %tr
      %th
      - for bike in the_bikes
        %th{:class =>"right"}
          - unless bike.decommissioned != nil
            %a{:href => "/bike/#{bike.name}/new_milage"} New            
      %th
  %tbody
    - row=1
    - _period = min
    - while _period < max          
    - _sum_period = 0
      %tr{:class => "row#{row=1-row}"}
        %td{:class => "period"}= _period.strftime('%Y-%m')
        - for bike in the_bikes do
        - _sum_period += (milage_in_period = bike.milage_in_period(_period))
          %td{:class =>"right"}= milage_in_period
        %th{:class =>"right sum"}= _sum_period
      - _period += 1.month

@@ index
-#-----------------------------
.page-header
  %h1
    Michis milage
    %img{:id => 'header_smilie', :src=>'/images/biker.gif', :alt => 'Biker Smilie'}
  .row
    .span3.biking_picture.hidden-phone
      %a{:id => 'a_biking_picture_link', :href => @a_biking_picture.link}
        %img{:id => 'a_biking_picture', :src => @a_biking_picture.url, :style => 'height:120px; border:1px solid #003366;', :alt => 'Michis biking pictures…'}
    .span9
      %p This is a little page about my biking activities. I like to track my milage and to take pictures of the places where i go. The biking pictures on the left are a subset from my pictures on <a href="http://dailyfratze.de/michael">dailyfratze.de</a>, all of which are tagged with "Radtour" (Bicycle Tour). The picture will refresh itself every few seconds.
      %p See more biking pictures at my facebook album <a href="http://www.facebook.com/album.php?aid=155553&amp;id=535518282&amp;l=6094f409e1">Biking 2010</a> or have a look at my collection of <a href="/tracks">tracks</a>.
      %p
        ="Since  #{[@min, @min_decommissioned].min.strftime '%d.%m.%Y'}, i pedalled"      
        %strong= "#{@sum_bikes + @sum_trips}km"
        ="on different bikes."

.row
  .span12
    .anchor{:id => 'current'}
      %h2 Current year
      %p This chart shows my current biking activities.
      .chart-container{:id => 'current-data'}

.row
  .span12
    .anchor{:id => 'history'}
      %h2 History
      %p Those are the overall statistics since 2009. You see a massive decline in 2011, when i exessivly programmed on <a href="http://dailyfratze.de">Daily Fratze 2</a>. I hope the trend in the last 2 months of 2012 continues… Biking tends to keep me sane ;)
      .chart-container{:id => 'historical-data'}

.row
  .span12
    .anchor{:id => 'details'}
      %h2 Details
      .row
        .span4
          %h3
            Bikes
            %small        
              %a{:href => '/new_bike'} New
          = haml :bike_table, :layout=>false, :locals => {:the_bikes => @bikes, :min => @min, :max => @max, :classes => 'use-in-charts'}        
        .span4
          %h3{:id => 'trips'} 
            Assorted Trips
            %small
              %a{:href => "/new_trip"} New
          %table{:class => "trip_table"}
            %thead
              %tr
                %th{:class => "left period"} When
                %th{:class =>"right"} Distance
            %tfoot
              %tr
                %th
                %th{:class => "right"}= @sum_trips
            %tbody
              - row=1
              - for trip in @trips
                %tr{:class => "row#{row=1-row}"}
                  %td{:class => "period"}= trip.when.strftime '%Y-%m-%d'
                  %td{:class => "right"}= trip.distance.to_f
        .span4
          %h3
            Decommissioned Bikes
          = haml :bike_table, :layout=>false, :locals => {:the_bikes => @decommissioned_bikes, :min => @min_decommissioned, :max => @max_decommissioned, :classes => 'use-in-charts'}
:javascript
  var createNestedObject = function( base, names, value ) {
    var lastName = arguments.length === 3 ? names.pop() : false;
    for( var i = 0; i < names.length; i++ )
      base = base[ names[i] ] = base[ names[i] ] || {};
    if( lastName ) base = base[ lastName ] = value;
    return base;
  };
  
  $(document).ready(function() {
    function callMeOften() {
      $('#header_smilie').attr("src","/images/waiting.gif");
      $.getJSON("/biking_picture",function(remote_result){
        $('#a_biking_picture_link').attr("href",remote_result.link);
        $('#a_biking_picture').attr("src",remote_result.url);
        $('#header_smilie').attr("src","/images/biker.gif");
      });
    }
    var holdTheInterval = setInterval(callMeOften, 8000);
  });              

@@ about
-#-----------------------------
.row
  .span12
    .anchor{:id => 'about'}
      %h2= @title_path
      %p This application is written in <a href="http://www.ruby-lang.org">Ruby</a> and uses <a href="http://www.sinatrarb.com/">Sinatra</a> and <a href="http://www.sqlite.org/">SQLite</a>.
      %p The chart is created using the amazing <a href="http://www.highcharts.com">Highcharts JS</a> library and the map is based on Google Maps.    
      %p I made heavily use of Sinatras inline templates and therefore the whole application is, although a full mvc based application, one single file still way under 1000 lines, including all model and css code.
      %p All view code is written using <a href="http://haml.hamptoncatlin.com/">Haml</a> respectively Sass, including the auto-refreshing JavaScript Code, so no XHTML pollutes this source file.
      %p
        The source code is available under the <a href="http://www.opensource.org/licenses/bsd-license.php">BSD License</a>:
      %p
        %a{:href => 'biking.rb'} biking.rb
      %p Most of the stuff is documented and apart from that, i've written some words right here:
      %p
        %a{:href => 'http://info.michael-simons.eu/2009/07/29/creating-a-self-containing-mvc-application-with-sinatra/'} Creating a self containing mvc application with Sinatra
      %p If you like this application or have any questions, just drop me a line at <a href="mailto:misi@planet-punk.de">misi@planet-punk.de</a>.
      %p
        %img{:id => 'somerights20', :src=>'/images/somerights20.gif', :alt => 'Some Rights Reserved', :style => 'vertical-align:text-top;'}
    
@@ new_bike
-#-----------------------------
.row
  .span12
    .anchor{:id => 'new_bike'}
      %h2= @title_path
      %form{:method => 'post', :action => "/bikes", :class => 'form-horizontal'}
        .control-group
          %label{:for => 'name', :class => 'control-label'} Name
          .controls
            %input{:type => 'text', :id => 'name', :name => 'name'}
        .control-group
          %label{:for => 'color', :class => 'control-label'} Color
          .controls
            %input{:type => 'text', :id => 'color', :name => 'color'}
        .form-actions
          %button{:type => 'submit', :class => 'btn btn-primary'} Save
          %a{:href => '/#details'}
            %button{:type => 'button', :class => 'btn'} Cancel

@@ new_milage
-#-----------------------------
.row
  .span12
    .anchor{:id => 'new_milage'}
      %h2= @title_path
      %form{:method => 'post', :action => "/bike/#{@bike.name}/milages", :class => 'form-horizontal'}
        .control-group
          %label{:for => 'milage_when', :class => 'control-label'} Date
          .controls
            %input{:type => 'text', :id => 'milage_when', :name => 'milage[when]', :type => 'date'}
        .control-group
          %label{:for => 'milage_value', :class => 'control-label'} Value
          .controls
            %input{:type => 'text', :id => 'milage_value', :name => 'milage[value]', :type => 'number'}
        .form-actions
          %button{:type => 'submit', :class => 'btn btn-primary'} Save
          %a{:href => '/#details'}
            %button{:type => 'button', :class => 'btn'} Cancel

@@ new_trip
-#-----------------------------
.row
  .span12
    .anchor{:id => 'new_trip'}
      %h2= @title_path
      %form{:method => 'post', :action => "/trips", :class => 'form-horizontal'}
        .control-group
          %label{:for => 'when', :class => 'control-label'} Date
          .controls
            %input{:type => 'text', :id => 'when', :name => 'when', :type => 'date'}
        .control-group
          %label{:for => 'distance', :class => 'control-label'} Distance
          .controls
            %input{:type => 'text', :id => 'distance', :name => 'distance', :type => 'number'}
        .form-actions
          %button{:type => 'submit', :class => 'btn btn-primary'} Save
          %a{:href => '/#details'}
            %button{:type => 'button', :class => 'btn'} Cancel          

@@ new_track
-#-----------------------------
.row
  .span12
    .anchor{:id => 'new_track'}
      %h2= @title_path
      %form{:method => 'post', :action => "/tracks", :enctype => "multipart/form-data", :class => 'form-horizontal'}
        .control-group
          %label{:for => 'when', :class => 'control-label'} Date
          .controls
            %input{:type => 'text', :id => 'when', :name => 'when', :type => 'date'}
        .control-group
          %label{:for => 'name', :class => 'control-label'} Name
          .controls
            %input{:type => 'text', :id => 'name', :name => 'name'}
        .control-group
          %label{:for => 'type', :class => 'control-label'} Type
          .controls
            %input{:type => 'text', :id => 'type', :name => 'type', :autocomplete => 'off', 'data-provide' => "typeahead", 'data-source' => "[#{Track::TYPES.map{|i| %Q("#{i}") }.join(',')}]", :value => "#{Track::TYPES[0]}"}
        .control-group
          %label{:for => 'description', :class => 'control-label'} Description
          .controls
            %textarea{:cols => 40, :rows => 10, :id => 'description', :name => 'description'}
        .control-group
          %label{:for => 'data', :class => 'control-label'} Trackdata
          .controls
            %input{:type => 'file', :id => 'data', :name => 'data'}
        .form-actions
          %button{:type => 'submit', :class => 'btn btn-primary'} Save
          %a{:href => '/tracks'}
            %button{:type => 'button', :class => 'btn'} Cancel                    
  
@@ tracks
-#-----------------------------
.row
  .span12
    .anchor{:id => 'tracks'}
      %h2= @title_path        
      %p
        %small        
          %a{:href => '/new_track'} New
      %table{:class => ["trip_table", "track_table"]}
        %thead
          %tr
            %th{:class => "left period"} When
            %th{:class =>"left"} Name
            %th{:class => "left"} Type
            %th{:class =>"right"} GPX
            %tbody
              - row=1
              - for track in @tracks
                %tr{:class => "row#{row=1-row}"}
                  %td{:class => "period"}= track.when.strftime '%d.%m.%Y'
                  %td{:class => "left"}
                    %a{:href => "tracks/#{track.pretty_id}"}= track.name
                  %td{:class => "left"}= track.type
                  %td{:class => "right"}
                    %a{:href => "tracks/#{track.pretty_id}.gpx"} ↓

@@ osm_js
-#-----------------------------
:javascript
  var map;
  $(document).ready(function() {
    if($('#map').height() == 0)
      $("#map").css('height', $("#map").width()*2/3);
            
    var home = #{@home.to_json};
  
    map = new OpenLayers.Map ("map", {
      controls:[
        new OpenLayers.Control.Navigation(),
        new OpenLayers.Control.PanZoomBar(),
        new OpenLayers.Control.LayerSwitcher(),
        new OpenLayers.Control.Attribution()
      ],
      projection: new OpenLayers.Projection("EPSG:900913"),
      displayProjection: new OpenLayers.Projection("EPSG:4326"),
      units: 'm'
    });
  
  	map.addLayer(new OpenLayers.Layer.OSM.Mapnik("Mapnik"));
  	map.addLayer(new OpenLayers.Layer.OSM.CycleMap("CycleMap"));
    var gpx = new OpenLayers.Layer.GML("#{@track.name}", "/tracks/#{@track.pretty_id}.gpx", {
      format: OpenLayers.Format.GPX,
      style: {strokeColor: "red", strokeWidth: 5, strokeOpacity: 1.0},
      projection: new OpenLayers.Projection("EPSG:4326")
    });
    map.addLayer(gpx);
    var size = new OpenLayers.Size(16,16);
    var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);
    var icon = new OpenLayers.Icon('http://simons.ac/images/favicon.png',size,offset);
    layerMarkers = new OpenLayers.Layer.Markers("Markers");
  	map.addLayer(layerMarkers);
    layerMarkers.addMarker(new OpenLayers.Marker(new OpenLayers.LonLat(home.longitude, home.latitude).transform(map.displayProjection, map.getProjectionObject()),icon));
  
    bounds = new OpenLayers.Bounds();
    bounds.extend(new OpenLayers.LonLat(#{@track.minlon}, #{@track.minlat}));
    bounds.extend(new OpenLayers.LonLat(#{@track.maxlon}, #{@track.maxlat}));
    map.zoomToExtent(bounds.transform(map.displayProjection, map.getProjectionObject()));
  });
    
@@ map
-#-----------------------------
.row
  .span12
    .anchor{:id => 'track'}
      %h2= "#{@track.when.strftime('%d.%m.%Y')}: #{@track.name} (#{@track.type})"
      #google
        #map{:style=>"width:100%; min-height:100%; border:1px solid black;"}
= haml :osm_js, :layout => false
  
@@ embedded_track
-#-----------------------------
%html
  %head
    %meta{'http-equiv' => 'Content-Type', :content=> 'text/html; charset=utf-8'}
    %meta{'name'       => 'Author',       :content=> 'Michael J. Simons'}
    %title biking.michael-simons.eu
    %link{:rel => 'stylesheet', :href => '/css/openlayersstyle.css', :type => 'text/css'}
    %link{:rel => 'stylesheet', :href => '/css/openlayersgoogle.css', :type => 'text/css'}    
    %link{:rel => 'icon', :href => '/images/favicon.ico', :type => 'image/x-icon'}    
    %script{:src => '/js/jquery.min.js', :type => 'text/javascript'}
    %script{:src => 'http://openlayers.org/api/2.11/OpenLayers.js', :type => 'text/javascript'}
    %script{:src => 'http://www.openstreetmap.org/openlayers/OpenStreetMap.js', :type => 'text/javascript'}    
  %body{:style=>"overflow:hidden; margin:0px; padding: 0px;"}
    #google
      #map{:style=>"width:#{@width-2}px; height: #{@height-2}px; border:1px solid black;"}
    = haml :osm_js, :layout => false