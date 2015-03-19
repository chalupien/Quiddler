<?php 

require 'kint-master/kint-master/Kint.class.php';
 

$query = $_GET['word'];
$file = file_get_contents('http://www.google.com/dictionary/json?callback=a&q='.$query.'&sl=en&tl=en&restrict=pr,de&client=te');
// var_dump($file);
$file = substr($file, 2, -10);
$file = preg_replace("/\\\x[0-9a-f]{2}/", "", $file);

$json = json_decode($file,true);


echo $json['primaries'][0]['terms'][0]['labels'][0]['text'] . "<Br><Br>";
echo $json['primaries'][0]['terms'][sizeof($json['primaries'][0]['terms'])-1]['text'] . "<Br><Br>";

echo var_dump($json);


	//$inst = new Words(getDefinition("hello"));

	//echo $inst; // echos 5

	class Words {
		
		public $file;
		public $words = array();
		public $reqs = array();
		
		public function getFromFile($file) {
			$this->file = file_get_contents($file);
			
			return $this->words = array_clean(explode("\n", $this->file));
		}
		
		private $settings = array(
			'url' => 'http://definr.com/definr/show/',
			'glob' => 'http://definr.com:4000/glob.json/',
			'oxford' => 'http://www.askoxford.com/concise_oed/'
		);
		public $definitions = array();
		
		public function getDefinitions($t = 'definr') {
			foreach($this->words as $word) {
				if($t == 'definr') {
					$definr = $this->getDefinition($word);
					$this->definitions[$definr['word']] = array('trans' => $this->getBabel($definr['word']), 'def' => $definr['def']);
				} else {
					$def = $this->getOxford($word);
					$this->definitions[$word] = array('trans' => $this->getBabel($word), 'def' => $def);
				}
			}
		}
		
		public function delNotFound() {
			$f = fopen('./list.txt', 'w');

			foreach($this->definitions as $n => $word) {
				fwrite($f, $n . "\n");
			}
			
			fclose($f);
		}
		
		private function getDefinition($word) {
			$url = $this->settings['url'] . $word;
			$res = $this->get($url);
			
			$res = str_replace(array('&nbsp;', "\nv ", "\n"), array(null, null, null), strip_tags($res));
			preg_match_all("/[\d\ n]+\:([a-z\s\',.\(\)\-]+)/ims", $res, $matches);
			
			if(stristr($res, 'Did you mean: ')) {
				$ops = explode(', ', $matches[1][0]);
				$op = array_map('strip_tags', $ops);
				
				if(count($op) == 1) {
					return $this->getDefinition($op[0]);
				}
				
				$tot = 100000;
				$nword = '';
				foreach($op as $w) {
					$lev = levenshtein($word, $w);
					
					if($lev < $tot && strlen($w) > 2) {
						$nword = $w;
					}
				}
				
				return $this->getDefinition($nword);
			}
			
			return (count($matches[1]) > 0)? array('word' => $word, 'def' => $matches[1]) : 'Not Found';
		}
		
		private function getOxford($word) {
			$url = $this->settings['oxford'] . $word;
			$res = $this->get($url);
			$regex = '/<b>[a-z^0-9]+<\/b>([^\#]+)/i';
			
			preg_match_all($regex, $res, $matches);
			
			$res = array();
			foreach($matches[1] as $match) {
				$str = str_replace(array('&nbsp;', '&'), array(null, null), strip_tags(trim($match)));
				preg_match_all('/[\d]\ ([a-z\s\,\(\)^0-9]+)/i', $str, $m);
				
				foreach($m[1] as $ma) {
					$res[] = $ma;
				}
			}
			
			return (count($res) > 0)? $res : 'none';
		}
		
		private function getTranslation($word) {
			
			$post = array(
				'hl' => 'en',
				'ie' => 'UTF8',
				'text' => $word
			);
			
			$post = implode('&', $this->arrayToString($post));
			
			$url = 'http://google.com/translate_t?langpair=en|nl';
			
			$c = curl_init($url);
			curl_setopt($c, CURLOPT_POST, true);
			curl_setopt($c, CURLOPT_POSTFIELDS, $post);
			curl_setopt($c, CURLOPT_RETURNTRANSFER, true);
			
			$res = curl_exec($c);
			
			preg_match('/<div id=result_box dir="ltr">([^\s]+)<\/div>/i', $res, $matches);
			
			return (isset($matches[1]) && !stristr($matches[1], $word))? $matches[1] : '';
			
		}
		
		private function getBabel($word) {
			$post = array(
				'ei' => 'UTF-8',
				'doit' => 'done',
				'fr' => 'gf-res',
				'intl' => '1',
				'tt' => 'urltext',
				'trtext' => $word,
				'lp' => 'en_nl',
				'btnTrTxt' => 'Translate'
			);
			
			$post = implode('&', $this->arrayToString($post));
			
			$url = 'http://babelfish.yahoo.com/translate_txt';
			
			$c = curl_init($url);
			curl_setopt($c, CURLOPT_POST, true);
			curl_setopt($c, CURLOPT_POSTFIELDS, $post);
			curl_setopt($c, CURLOPT_RETURNTRANSFER, true);
			
			$res = curl_exec($c);
			
			preg_match('/<div id="result"><div style="padding:0.6em;">([a-zA-Z0-9]+)<\/div><\/div>/i', $res, $matches);
			
			return (isset($matches[1]) && !stristr($matches[1], $word))? $matches[1] : '';
		}
		
		private function arrayToString(array $array) {
			$arr = array();
			foreach($array as $k => $v) {
				$arr[] = urlencode($k) . '=' . urlencode($v);
			}
			return $arr;
		}

		
		private function get($url) {
			$tmpCon = curl_init($url);
			
			// set curl to return the transfer
			curl_setopt($tmpCon, CURLOPT_RETURNTRANSFER, true);
			
			$result = curl_exec($tmpCon);
			
			// cleanup
			curl_close($tmpCon);
			unset($tmpCon);
			
			$this->reqs[] = $url;
			
			return $result;
		}
	
	}

?>