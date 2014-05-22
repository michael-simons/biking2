/*
 * Copyright 2014 Michael J. Simons.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ac.simons.biking2.jobs;

import ac.simons.biking2.config.PersistenceConfig;
import ac.simons.biking2.persistence.entities.BikingPicture;
import ac.simons.biking2.persistence.repositories.BikingPictureRepository;
import ac.simons.biking2.rss.RSSDateTimeAdapter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static java.time.ZonedDateTime.of;
import static java.util.Calendar.getInstance;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 * @author Michael J. Simons
 */
public class FetchBikingPicturesJobTest {
    @Rule
    public final ExpectedException expectedException = none();

    private final RSSDateTimeAdapter dateTimeAdapter = new RSSDateTimeAdapter();
    private final File tmpDir;

    public FetchBikingPicturesJobTest() {
	this.tmpDir = new File(System.getProperty("java.io.tmpdir"), Long.toString(System.currentTimeMillis()));
	this.tmpDir.deleteOnExit();
    }

    @Test
    public void jobShouldAbortWhenOlderItemsThanMaxPubDateAreFoundOnPage1() throws Exception {
	final DailyFratzeProvider dailyFratzeProvider = mock(DailyFratzeProvider.class);
	stub(dailyFratzeProvider.getRSSConnection(null)).toReturn(this.getClass().getResource("/biking_pictures.rss").openConnection());

	final BikingPictureRepository bikingPictureRepository = mock(BikingPictureRepository.class);
	stub(bikingPictureRepository.getMaxPubDate()).toReturn(GregorianCalendar.from(dateTimeAdapter.unmarshal("Sat, 07 Sep 2013 18:43:48 GMT")));

	final FetchBikingPicturesJob job = new FetchBikingPicturesJob(dailyFratzeProvider, bikingPictureRepository, tmpDir);

	final List<BikingPicture> toDownload = job.createDownloadList();

	assertThat(toDownload.size(), is(equalTo(5)));
	verify(dailyFratzeProvider).getRSSConnection(null);
    }

    @Test
    public void jobShouldAbortWhenOlderItemsThanMaxPubDateAreFoundOnPage2() throws Exception {
	final DailyFratzeProvider dailyFratzeProvider = mock(DailyFratzeProvider.class);
	stub(dailyFratzeProvider.getRSSConnection(null)).toReturn(this.getClass().getResource("/biking_pictures.rss").openConnection());
	final String page2 = "http://dailyfratze.de/michael/tags/Theme/Radtour?format=rss&dir=desc&page=2";
	stub(dailyFratzeProvider.getRSSConnection(page2)).toReturn(this.getClass().getResource("/biking_pictures.2.rss").openConnection());
	final BikingPictureRepository bikingPictureRepository = mock(BikingPictureRepository.class);
	stub(bikingPictureRepository.getMaxPubDate()).toReturn(GregorianCalendar.from(dateTimeAdapter.unmarshal("Sun, 08 May 2011 18:38:25 GMT")));

	final FetchBikingPicturesJob job = new FetchBikingPicturesJob(dailyFratzeProvider, bikingPictureRepository, tmpDir);

	final List<BikingPicture> toDownload = job.createDownloadList();

	assertThat(toDownload.size(), is(equalTo(46)));
	verify(dailyFratzeProvider).getRSSConnection(null);
	verify(dailyFratzeProvider).getRSSConnection(page2);
    }
    
    @Test
    public void jobShouldHandleExceptionsGracefully() throws MalformedURLException, IOException, Exception {
	final DailyFratzeProvider dailyFratzeProvider = mock(DailyFratzeProvider.class);
	stub(dailyFratzeProvider.getRSSConnection(null)).toReturn(new File("/i/dont/exists").toURI().toURL().openConnection());
	
	final BikingPictureRepository bikingPictureRepository = mock(BikingPictureRepository.class);
	stub(bikingPictureRepository.getMaxPubDate()).toReturn(GregorianCalendar.from(dateTimeAdapter.unmarshal("Sun, 08 May 2011 18:38:25 GMT")));	
	
	final FetchBikingPicturesJob job = new FetchBikingPicturesJob(dailyFratzeProvider, bikingPictureRepository, tmpDir);
	final List<BikingPicture> toDownload = job.createDownloadList();
	assertThat(toDownload.size(), is(equalTo(0)));
    }

    @Test
    public void jobShouldThrowExceptionOnInvalidStorageDir() throws IOException {
	final DailyFratzeProvider dailyFratzeProvider = mock(DailyFratzeProvider.class);	
	final BikingPictureRepository bikingPictureRepository = mock(BikingPictureRepository.class);
	
	final File someFile = new File(System.getProperty("java.io.tmpdir"), "deleteMeReally");
	someFile.deleteOnExit();
	Assert.assertTrue(someFile.createNewFile());
	
	expectedException.expect(RuntimeException.class);
	expectedException.expectMessage("Could not create bikingPicturesStorage!");
	new FetchBikingPicturesJob(dailyFratzeProvider, bikingPictureRepository, someFile);
    }
    
    @Test
    public void jobShouldAbortWhenNoMorePagesAreAvailable() throws Exception {
	final DailyFratzeProvider dailyFratzeProvider = mock(DailyFratzeProvider.class);
	stub(dailyFratzeProvider.getRSSConnection(null)).toReturn(this.getClass().getResource("/biking_pictures.rss").openConnection());
	final String page2 = "http://dailyfratze.de/michael/tags/Theme/Radtour?format=rss&dir=desc&page=2";
	stub(dailyFratzeProvider.getRSSConnection(page2)).toReturn(this.getClass().getResource("/biking_pictures.2.rss").openConnection());
	final BikingPictureRepository bikingPictureRepository = mock(BikingPictureRepository.class);
	stub(bikingPictureRepository.getMaxPubDate()).toReturn(GregorianCalendar.from(of(2000, 1, 1, 21, 21, 00, 0, ZoneId.systemDefault())));
	final FetchBikingPicturesJob job = new FetchBikingPicturesJob(dailyFratzeProvider, bikingPictureRepository, tmpDir);

	final List<BikingPicture> toDownload = job.createDownloadList();

	assertThat(toDownload.size(), is(equalTo(48)));
	verify(dailyFratzeProvider).getRSSConnection(null);
	verify(dailyFratzeProvider).getRSSConnection(page2);
	verify(dailyFratzeProvider, times(0)).getRSSConnection("http://dailyfratze.de/michael/tags/Theme/Radtour?format=rss&dir=desc&page=3");
    }

    @Test
    public void shouldDownloadStuff() throws Exception {

	final DailyFratzeProvider dailyFratzeProvider = mock(DailyFratzeProvider.class);
	stub(dailyFratzeProvider.getImageConnection(45644)).toReturn(this.getClass().getResource("/45644.jpg").openConnection());
	stub(dailyFratzeProvider.getImageConnection(45325)).toReturn(this.getClass().getResource("/45325.jpg").openConnection());

	// Failure on connection
	URLConnection mockConnection = mock(URLConnection.class);
	stub(mockConnection.getInputStream()).toThrow(new IOException("ignoreMe"));
	stub(dailyFratzeProvider.getImageConnection(44142)).toReturn(mockConnection);

	stub(dailyFratzeProvider.getImageConnection(43461)).toReturn(null);

	// One picture exists
	final BikingPicture existingPicture = new BikingPicture("http://dailyfratze.de/fratzen/m/45325.jpg", dateTimeAdapter.unmarshal("Mon, 30 Dec 2013 23:18:35 GMT"), "http://dailyfratze.de/michael/2013/12/30");
	final BikingPictureRepository bikingPictureRepository = mock(BikingPictureRepository.class);
	stub(bikingPictureRepository.findByExternalId(45325)).toReturn(existingPicture);

	final FetchBikingPicturesJob job = new FetchBikingPicturesJob(dailyFratzeProvider, bikingPictureRepository, tmpDir);

	final List<BikingPicture> downloadList = Arrays.asList(
		new BikingPicture("http://dailyfratze.de/fratzen/m/45644.jpg", dateTimeAdapter.unmarshal("Sun, 12 Jan 2014 21:40:25 GMT"), "http://dailyfratze.de/michael/2014/1/12"),
		existingPicture,
		new BikingPicture("http://dailyfratze.de/fratzen/m/44142.jpg", dateTimeAdapter.unmarshal("Sun, 03 Nov 2013 21:01:01 GMT"), "http://dailyfratze.de/michael/2013/11/3"),
		new BikingPicture("http://dailyfratze.de/fratzen/m/43461.jpg", dateTimeAdapter.unmarshal("Sat, 05 Oct 2013 18:13:20 GMT"), "http://dailyfratze.de/michael/2013/10/3")
	);

	final List<BikingPicture> downloaded = job.download(downloadList);

	assertThat(downloaded.size(), is(equalTo(1)));
	assertThat(new File(this.tmpDir, PersistenceConfig.BIKING_PICTURES_DIRECTORY + "/45644.jpg").isFile(), is(true));
	assertThat(new File(this.tmpDir, PersistenceConfig.BIKING_PICTURES_DIRECTORY + "/45325.jpg").isFile(), is(false));
	assertThat(new File(this.tmpDir, PersistenceConfig.BIKING_PICTURES_DIRECTORY + "/44142.jpg").isFile(), is(false));
	assertThat(new File(this.tmpDir, PersistenceConfig.BIKING_PICTURES_DIRECTORY + "/43461.jpg").isFile(), is(false));
	// Verify number of calls with arbitrary ints
	verify(dailyFratzeProvider, times(3)).getImageConnection(anyInt());
    }
    
    @Test
    public void runMethodShouldWorkAsExpected() throws IOException {
	final DailyFratzeProvider dailyFratzeProvider = mock(DailyFratzeProvider.class);
	stub(dailyFratzeProvider.getRSSConnection(null)).toReturn(this.getClass().getResource("/biking_pictures.rss").openConnection());
	
	final BikingPictureRepository bikingPictureRepository = mock(BikingPictureRepository.class);	
	stub(bikingPictureRepository.getMaxPubDate()).toReturn(getInstance());	
	
	final FetchBikingPicturesJob job = new FetchBikingPicturesJob(dailyFratzeProvider, bikingPictureRepository, tmpDir);
	job.run();
	
	verify(dailyFratzeProvider).getRSSConnection(null);
	verify(bikingPictureRepository).getMaxPubDate();
	
	Mockito.verifyNoMoreInteractions(dailyFratzeProvider, bikingPictureRepository);
	
    }
}
