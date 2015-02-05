<?

class Api {

	private $address;

	function __construct($address = NULL) {
		if (is_null($address)) {
			throw new Exception("Address must not be null");
		}

		$this->address = $address;
	}

	public function getAddress() {
		return $this->address;
	}

	public function fetchJson() {
		return json_decode($this->fetchRaw());
	}

	public function fetchRaw() {
		return file_get_contents($this->address);
	}

}

?>