///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17
//DEPS com.drewnoakes:metadata-extractor:2.15.0
//DEPS info.picocli:picocli:4.5.2
//DEPS net.coobird:thumbnailator:0.4.13
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Dithering;
import net.coobird.thumbnailator.resizers.configurations.Rendering;

@Command(name = "createGalleries", description = "Creates a gallery page for every year with images.")
public class CreateGalleries implements Callable<Integer> {

	record Image(Path path, LocalDateTime takenOn, GeoLocation location) {

		static Optional<Image> fromPath(Path path) {
			try {
				var metadata = ImageMetadataReader.readMetadata(Files.newInputStream(path));

				var takenOn = metadata.getDirectoriesOfType(ExifSubIFDDirectory.class)
					.stream()
					.map(d -> d.getDateOriginal())
					.filter(d -> d != null)
					.map(d -> d.toInstant().atZone(ZoneId.of("Europe/Berlin")).toLocalDateTime())
					.findFirst().get();
				var geolocation = metadata.getDirectoriesOfType(GpsDirectory.class)
					.stream()
					.map(GpsDirectory::getGeoLocation)
					.filter(l -> !(l == null || l.isZero()))
					.findFirst();
				return Optional.of(new Image(path, takenOn, geolocation.orElse(null)));
			} catch (ImageProcessingException | NoSuchElementException | IOException e) {
				// We just ignore this image
			}
			return Optional.empty();
		}

		void resize(int width, Path name) throws IOException {
			Thumbnails.of(path.toFile())
				.width(width)
				.outputQuality(0.95)
				.outputFormat("jpg")
				.rendering(Rendering.QUALITY)
				.dithering(Dithering.DISABLE)
				.toFile(name.toFile());
		}

		String store(Integer index, Path outputFolder) throws IOException, InterruptedException {

			var baseNameFormatter = DateTimeFormatter.ofPattern("yyyy/yyyy-MM-dd-'%d-%s.jpg'", Locale.ROOT);
			var baseNameFormat = baseNameFormatter.format(takenOn);

			var thumb = outputFolder.resolve(String.format(Locale.ROOT, baseNameFormat, index, "thumb"));
			resize(640, thumb);

			var full = outputFolder.resolve(String.format(Locale.ROOT, baseNameFormat, index, "full"));
			resize(1280, full);

			var linkToMaps = location == null ? "" :
				String.format(Locale.ROOT,
					"<a href='https://www.google.com/maps/search/?api=1&query=%1$.4f,%2$.4f'>Taken at %1$.4f, %2$.4f</a>",
					location.getLatitude(), location.getLongitude());

			var ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT).format(takenOn);
			var template = """
				<article class="thumb">
					<a href="%s" class="image"><img src="%s" alt="" /></a>
					<h2>%s</h2>
					<p>%s</p>
				</article>
				""";
			return String
				.format(Locale.ROOT, template, full.getFileName().toString(), thumb.getFileName().toString(), ymd,
					linkToMaps);
		}
	}

	@Parameters(index = "0", description = "Input folder")
	private Path inputFolder;

	@Parameters(index = "1", description = "Output folder")
	private Path outputFolder;

	private void prepareOutputFolder(Collection<Integer> years) throws IOException {
		if (Files.isDirectory(outputFolder)) {
			Files.walkFileTree(outputFolder, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		}

		Files.deleteIfExists(outputFolder);
		for (Integer year : years) {
			Files.createDirectories(outputFolder.resolve(Path.of(year.toString())));
		}
	}

	@Override
	public Integer call() throws Exception {

		if (!Files.isDirectory(inputFolder)) {
			return ExitCode.USAGE;
		}

		var imagesPerYear = Files.walk(inputFolder)
			.filter(
				p -> Files.isRegularFile(p) && p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jpg"))
			.map(Image::fromPath)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.sorted(Comparator.comparing(Image::takenOn))
			.collect(Collectors.groupingBy(i -> i.takenOn().getYear()));

		prepareOutputFolder(imagesPerYear.keySet());

		imagesPerYear.forEach((year, images) -> {
			var content = new StringBuilder();
			var count = new AtomicInteger(0);
			images.forEach(i -> {
				try {
					var index = count.incrementAndGet();
					content.append(i.store(index, outputFolder));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			try {
				Files.write(outputFolder.resolve(Path.of(year.toString(), "index.html")),
					PAGE_TEMPLATE
						.replaceAll("\\$year", year.toString())
						.replaceAll("\\$content", content.toString())
						.getBytes(StandardCharsets.UTF_8)
				);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});

		return ExitCode.OK;
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new CreateGalleries()).execute(args);
		System.exit(exitCode);
	}

	private static final String PAGE_TEMPLATE = """
		<!DOCTYPE HTML>
		<!--
			Multiverse by HTML5 UP
			html5up.net | @ajlkn
			Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
		-->
		<html>
			<head>
				<title>Gallery $year | biking.michael-simons.eu</title>
				<meta charset="utf-8" />
				<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
				<link rel="stylesheet" href="/css/gallery.css" />
				<noscript><link rel="stylesheet" href="/css/noscript.css" /></noscript>
			</head>
			<body class="is-preload">

				<div id="wrapper">

					<header id="header">
						<h1><a href="index.html"><strong>Gallery $year</a></h1>
						<nav><ul><li><a href="#footer" class="icon solid fa-info-circle">About</a></li></ul></nav>
					</header>

					<div id="main">
		  				$content
					</div>

					<footer id="footer" class="panel">
						<div class="inner split">
							<div>
								<section>
									<h2>Part of biking.michael-simons.eu</h2>
									<p>
										This gallery is part of <a href="https://biking.michael-simons.eu">biking.michael-simons.eu</a>. The full source code of this application is available on <a href="https://github.com/michael-simons/biking2">GitHub</a>,\s
										including the generator used for building this gallery.<br />
										Gallery created with <a href="https://github.com/michael-simons/biking2/blob/public/src/cli/CreateGalleries.java">CreateGalleries.java</a>,
										a small Java CLI powered by <a href="https://www.jbang.dev">jbang</a> and the mighty <a href="https://drewnoakes.com/code/exif/">Metadata Extractor</a>.
									</p>
								</section>
								<p class="copyright">
									&copy; 2020 by Michael J. Simons, Design by <a href="http://html5up.net">HTML5 UP</a>.<br />
									While the sourcecode of this application and the gallery generator is licensed under Apache-2.0 License,
									the images are published under <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/">Attribution-NonCommercial-ShareAlike 4.0 International</a>.
								</p>
							</div>
							<div>
								<section>
									<h2>Follow me on ...</h2>
									<ul class="icons">
										<li><a href="https://twitter.com/rotnroll666"   class="icon brands fa-twitter"><span class="label">Twitter</span></a></li>
										<li><a href="https://github.com/michael-simons" class="icon brands fa-github"><span class="label">GitHub</span></a></li>
										<li><a href="https://www.linkedin.com/in/michael-simons-196712139/" class="icon brands fa-linkedin-in"><span class="label">LinkedIn</span></a></li>
									</ul>
								</section>
							</div>
						</div>
					</footer>
				</div>

				<script src="/js/gallery/jquery.min.js"></script>
				<script src="/js/gallery/jquery.poptrox.min.js"></script>
				<script src="/js/gallery/browser.min.js"></script>
				<script src="/js/gallery/breakpoints.min.js"></script>
				<script src="/js/gallery/util.js"></script>
				<script src="/js/gallery/main.js"></script>
			</body>
		</html>
		""";
}
