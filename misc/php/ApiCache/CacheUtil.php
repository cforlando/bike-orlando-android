<?

class CacheUtil {

    // Default timeout is one hour
	const DEFAULT_TIMEOUT = 60 * 60;

	private $cacheFile;
	private $cacheTimeout;
	private $fp;

	function __construct($cacheFile = NULL, $cacheTimeout = self::DEFAULT_TIMEOUT) {
		if (is_null($cacheFile)) {
			throw new Exception("Cache file must not be null");
		}

		$this->cacheFile = $cacheFile;
		$this->cacheTimeout = $cacheTimeout;
	}

	public function exists() {
		return file_exists($this->cacheFile);
	}

	public function isExpired() {
		return time() - $this->lastModified() > $this->getTimeout();
	}

	public function lastModified() {
		return filemtime($this->cacheFile);
	}

	public function getTimeout() {
		return $this->cacheTimeout;
	}

	public function read() {
		$handle = fopen($this->cacheFile, "r");
		if ($handle === false) {
			throw new Exception("Failed to open cache file {$this->cacheFile}.");
		} else {
			while (($buffer = fgets($handle, 4096)) !== false) {
				echo $buffer;
			}
			fclose($handle);
		}
	}

	public function write($data) {
		if ($this->isLocked() === false) {
			throw new Exception("Lock must be obtained to write.");
		}

		$fp = fopen($this->cacheFile, "w");
		fwrite($fp, $data);
		fclose($fp);
	}

	public function isLocked() {
		return $this->fp !== NULL;
	}

	public function lock() {
		if ($this->isLocked()) {
			throw new Exception("Lock already obtained.");
		}

		$this->fp = fopen($this->lockFile(), "w+");
		if (flock($this->fp, LOCK_EX) === false) {
			throw new Exception("Lock failed.");
		}
	}

	public function unlock() {
		if (!$this->isLocked()) {
			throw new Exception("Lock does not exist.");
		}

		flock($this->fp, LOCK_UN);
		fclose($this->fp);
		unlink($this->lockFile());
		$this->fp = NULL;
	}

	private function lockFile() {
		return $this->cacheFile . ".lock";
	}

}

?>