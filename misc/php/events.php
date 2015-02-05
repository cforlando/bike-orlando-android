<?

//error_reporting(E_ALL);

// Enable gzip/deflate/none automagically
ob_start("ob_gzhandler");

// All returned data will be json unless something really bad happens
header('Content-Type: application/json');

function errorHandler($errno, $errstr) {
	http_response_code(500);
	die("{\"error\":\"$errstr\"}");
}

function shutdownHandler() {
	$errfile = "unknown file";
	$errstr  = "shutdown";
	$errno   = E_CORE_ERROR;
	$errline = 0;

	$error = error_get_last();

	if ($error !== NULL) {
		$errno   = $error["type"];
		$errfile = $error["file"];
		$errline = $error["line"];
		$errstr  = $error["message"];
		errorHandler($errno, $errstr);
	}
}

function exceptionHandler($exception) {
	errorHandler(0, $exception->getMessage());
}

// Set an expiration of the data 24 hours from now
function expiresHeader() {
	header('Expires: '.gmdate('D, d M Y H:i:s \G\M\T', time() + 86400));
}

// Handle practically any error by returning a json object with a String 'error'
set_exception_handler("exceptionHandler");
set_error_handler("errorHandler");
register_shutdown_function("shutdownHandler");

// Require the api utilities
require_once('ApiCache/CacheUtil.php');
require_once('ApiCache/Api.php');

// API addresses to hit
$addresses = [
	"https://webservices.secure-tix.com/rest/v2/getJsonEvents?userId=300&venueId=11807"
	, "https://webservices.secure-tix.com/rest/v2/getJsonEvents?userId=300&venueId=8513"
	];

// Cache location
$cacheFile = "cache/events.cache";

// Instantiate the caching utility
$cacheUtil = new CacheUtil($cacheFile);

if ($cacheUtil->exists() && !$cacheUtil->isExpired()) {
	// Cache exists and has not expired, dump the cache
	expiresHeader();
	$cacheUtil->read();
} else {
	// Obtain a lock to perform the update
	$cacheUtil->lock();

	// Recheck cache
	if ($cacheUtil->exists() && !$cacheUtil->isExpired()) {
		// Cache was updated while waiting for a lock, dump and die
		$cacheUtil->read();
		die();
	}

	// Combine both data sources
	$output = new stdClass;
	$output->venues = array();
	$output->events = array();

	// Read each address, strip useless data, and combine events on a key of 'venueId'
	foreach ($addresses as $address) {
		$api = new Api($address);
		$events = $api->fetchJson()->events;
		foreach ($events as $event) {
			// Grab the venue information before deleting it
			$venue = $event->venue;

			// Remove useless fields
			unset($event->venue);
			unset($event->performers);
			unset($event->eventDate);
			unset($event->listingCount);

			// Save the venue if not set
			if (!isset($output->venues[$venue->venueId])) {
				$output->venues[$venue->venueId] = $venue;
			}

			// Add the event to the venue key
			if (!isset($output->events[$venue->venueId])) {
				$output->events[$venue->venueId] = array();
			}
			$output->events[$venue->venueId][] = $event;
		}
	}

	// Encode the data to a json string
	$encodedOutput = json_encode($output);

	// Update the cache file
	$cacheUtil->write($encodedOutput);

	// Release the obtained lock
	$cacheUtil->unlock();

	// Finally dump the updated cache
	expiresHeader();
	echo $encodedOutput;
}

?>