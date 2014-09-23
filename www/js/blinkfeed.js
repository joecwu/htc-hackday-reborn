
var blinkfeed = {
	HIGHLIGHT_URL: "https://geo-prism.htcsense.com/s6/fi/hl/r/50?cids=1319108,1320486,1398121",
	fillItem: function(items,index) {
		var item = items[index];
		$('div.bf_title').text(item.meta.tl);
		var imgId = item.meta.th.id;
		var imgRs = item.meta.th.rs[0];
		var imgURL = 'https://img-prism.htcsense.com/thumbnail?id='+imgId+'&w='+imgRs;
		var iconURL = 'https://img-prism.htcsense.com/provider/icon?id='+item.meta.pid;
		$('div.bf_image').css("background-image", "url("+imgURL+")");
		// $('div.bf_image').prepend('<img class="bf_img" src="'+imgURL+'" />');
		$('div.bf_icon').css("background-image", "url("+iconURL+")");
		$('div.bf_source').text(item.meta.src);
		if(items.length > index+1){
			setTimeout( function() { blinkfeed.fillItem(items,index+1); } , 5000);
		}else{
			setTimeout( function() { blinkfeed.fillItem(items,0); } , 5000);
		}
	},
	getHighlight: function() {
		//alert(this.HIGHLIGHT_URL);
		var jqxhr = $.get( this.HIGHLIGHT_URL, function(dataStr) {
			var data = $.parseJSON(dataStr);
		  	var size = data.res.items.length;
		  	// alert( "success - " + size );
		  	blinkfeed.fillItem(data.res.items,0);
		  	return data;
		})
		  .fail(function(e) {
		    //var defaultData = $.parseJSON('{"res":{"items":[]}}');
		    var defaultData = $.parseJSON('{"res":{"items":[{"meta":{"id":"MR:29195882","u":"http://www.ibtimes.com/pope-francis-condemns-islamic-extremist-groups-pervert-religion-1692556?ft=61pb1","src":"International Business Times","tl":"Pope Francis Condemns Islamic Extremist Groups In Speech","ts":1411326960009,"pid":1299,"tp":1,"a":"International Business Times","img":[{"id":"20140921/origin/9cc01cde4409218d60bdc71d38e0ee3147aee437b819f3ad851ca909e28768ce--.jpeg","w":2200,"h":993,"c":"","mid":"#1"}],"v":null,"th":{"id":"20140921/1b90d4262f9d6d33f773e12cd6ab400c4886394269e4d098e1f34a18f7a9898e.jpeg","rs":["1080","720","540","360","230"],"mid":"#1"},"tids":["1319108"],"fg":0,"int":null,"bth":[{"id":"20140921/1b90d4262f9d6d33f773e12cd6ab400c4886394269e4d098e1f34a18f7a9898e.jpeg","rs":[{"rs":"1080","w":1080,"h":487},{"rs":"720","w":720,"h":324},{"rs":"540","w":540,"h":243},{"rs":"360","w":360,"h":162},{"rs":"230","w":230,"h":103}],"mid":"#1"}],"adv":null,"in_img":1}},{"meta":{"id":"MR:29195779","u":"http://au.ibtimes.com/articles/566971/20140921/pirate-bay-21-virtual-machines-operations.htm?fs=4fc87","src":"International Business Times","tl":"The Pirate Bay Operations: 21 Virtual Machines Are Used To Run The File-sharing Website","ts":1411326660007,"pid":1299,"tp":1,"a":"International Business Times","img":[{"id":"20140921/origin/e9ca7e826bf8be03b86499452787597fe5cf27b1b0c97b653ce15bfa08d2cdbc--.jpeg","w":950,"h":678,"c":"","mid":"#1"}],"v":null,"th":{"id":"20140921/2605d8f2423b4e4dcd554c947d38cdb32aeecd3c511e270af391b0947afc24a8.jpeg","rs":["720","540","360","230"],"mid":"#1"},"tids":["1320486"],"fg":0,"int":null,"bth":[{"id":"20140921/2605d8f2423b4e4dcd554c947d38cdb32aeecd3c511e270af391b0947afc24a8.jpeg","rs":[{"rs":"720","w":720,"h":513},{"rs":"540","w":540,"h":385},{"rs":"360","w":360,"h":256},{"rs":"230","w":230,"h":164}],"mid":"#1"}],"adv":null,"in_img":1}},{"meta":{"id":"MR:29195681","u":"http://gizmodo.com/star-wars-producers-wanted-a-fully-operational-drone-de-1637404816","src":"Gizmodo","tl":"Star Wars Producers Wanted a Fully Operational Drone Defense System","ts":1411326360010,"pid":1423,"tp":1,"a":"Gizmodo","img":[{"id":"20140921/origin/d9d59e24df3fa72489b718551918ff04c82b0739c6f6905a8f884218a09ccf1b--.jpeg","w":636,"h":288,"c":"","mid":"#1"},{"id":"20140921/origin/d9f23411b3c50c467108962a998bc608a3cf0c1cd398f9a9e8404fca4bb36ba3--.jpeg","w":635,"h":320,"c":"","mid":"#2"}],"v":null,"th":{"id":"20140921/1fec85467b1e0d760459fb4d9406a3fde22d6705536f9f671059b4988f0667a5.jpeg","rs":["540","360","230"],"mid":"#1"},"tids":["1320486"],"fg":0,"int":null,"bth":[{"id":"20140921/1fec85467b1e0d760459fb4d9406a3fde22d6705536f9f671059b4988f0667a5.jpeg","rs":[{"rs":"540","w":540,"h":244},{"rs":"360","w":360,"h":163},{"rs":"230","w":230,"h":104}],"mid":"#1"},{"id":"20140921/9c930e547a4f972d58345b64c82d1cdbbf58ee7fdbde267fe06bb3d4a0eb8b5d.jpeg","rs":[{"rs":"540","w":540,"h":272},{"rs":"360","w":360,"h":181},{"rs":"230","w":230,"h":115}],"mid":"#2"}],"adv":null,"in_img":1}},{"meta":{"id":"MR:29191947","u":"http://www.appledaily.com.tw/mobile/rnewsarticle/artid/473706/issueid/20140922/","src":"台灣蘋果日報","tl":"狗狗14個內心OS......根本全寫臉上了好嗎!","ts":1411316940019,"pid":1119,"tp":1,"a":"台灣蘋果日報","img":[{"id":"20140921/origin/9401156b8051ea69af44d03289c063ad49976f5fd0a50f2241507cefa34eb480--.jpeg","w":420,"h":386,"c":"喔喔喔喔喔！你永遠不會理解這有多......好玩！","mid":"#1"},{"id":"20140921/origin/ae70918937ffa02ba64d29104567385e3085243d83c0afe7244903646dd01f36--.jpeg","w":420,"h":416,"c":"每次你說我該洗澡的時候，我都覺得是不是你鼻子有問題。","mid":"#10"},{"id":"20140921/origin/babec60dfe5ae7614f198f5182ab620264ad7c3ac655997db3d5c0ef0220b896--.jpeg","w":420,"h":415,"c":"雖然你說不喜歡我舔你的臉，但......你也拿我沒轍不是嘛哈哈哈哈哈！","mid":"#11"},{"id":"20140921/origin/da8e738c3a9c1bea09d80452380b1ab06838708cc63ea03278211079a28957fd--.jpeg","w":420,"h":417,"c":"我知道你有幫我買小床，但世界上哪有比狗狗打呼更悅耳的聲音？所以我還是要跟你一起睡。","mid":"#12"},{"id":"20140921/origin/7b59c1655bb745dcc3fe596c3d9cf8df819c9a113d6d6891f9d82cd1fe86cef5--.jpeg","w":420,"h":420,"c":"看獸醫簡直是夢靨！但你陪在我身邊我就有勇氣。","mid":"#13"},{"id":"20140921/origin/a3f424791a73ff7063512aaed7dc9e73fa803b6d2641e2641f735eebec5fd977--.jpeg","w":420,"h":421,"c":"我知道我是個闖禍精，但你對我來說超級重要，比培根還重要（這絕對是夢話）","mid":"#14"},{"id":"20140921/origin/0dbcd138f2f8c5a119d425c4d6122a4b93e65f1d71b443f0402d512d09f5a278--.jpeg","w":420,"h":544,"c":"我知道一天不能吃太多零食，但我知道你一定無法拒絕我這張臉。","mid":"#2"},{"id":"20140921/origin/164819be1a736be58e7490fc7705ec9615fe754edec2879e3a7df2121b27e79d--.jpeg","w":420,"h":420,"c":"不准用拼音ㄙ～ㄢ～ˋ～ㄅ～ㄨ～ˋ～來欺騙我的感情，你明明只教會我聽散步！","mid":"#3"},{"id":"20140921/origin/18d32adbe557fd861d50b7401f900e4996f50cc016677d9430cb73b115b3440c--.jpeg","w":420,"h":418,"c":"對啦對啦～我已經有玩具有狗窩有朋友，但你才是我的全世界！","mid":"#4"},{"id":"20140921/origin/53c6e07d1d2857b1d52cbac096a584ea35b5b5d8e01b85542d0eda21eacada6a--.jpeg","w":420,"h":422,"c":"趁你出門時抓爛你的家具，都是因為太想你。","mid":"#5"},{"id":"20140921/origin/e3cc7f4bbc0a2c3520c9fab3e11b514c90d7b9834e6e72318995590788167b32--.jpeg","w":420,"h":420,"c":"關於那些神秘失蹤的襪子......我都藏在秘密基地裡了。","mid":"#6"},{"id":"20140921/origin/b6e90b7994462551dfb7146d15a5be7c8ad448e0cf264adb1e069b003d208719--.jpeg","w":420,"h":417,"c":"你看到這噁爛的東東了嗎？這是我給你的禮物！因為我愛你呀～","mid":"#7"},{"id":"20140921/origin/967cd56e337cd074398c541328e93644d26e027e170337df93e46e3958ea99b5--.jpeg","w":420,"h":386,"c":"別太感謝我，這鞋子已經退流行了。","mid":"#8"},{"id":"20140921/origin/e00d6f2b4ddf91f211fba8fc60f4de94b4eee5fc6e8820dd8b3d257edc7e0c48--.jpeg","w":420,"h":544,"c":"雖然我知道怎麼玩你丟我撿，但我覺得你追著我跑的樣子更好玩。","mid":"#9"}],"v":null,"th":{"id":"20140921/fcc574d75455d3ad787caaf7619dfc1e5c265bc8235664a404eb6774b1defe02.jpeg","rs":["360","230"],"mid":"#1"},"tids":["1398121"],"fg":0,"int":null,"bth":[{"id":"20140921/fcc574d75455d3ad787caaf7619dfc1e5c265bc8235664a404eb6774b1defe02.jpeg","rs":[{"rs":"360","w":360,"h":330},{"rs":"230","w":230,"h":211}],"mid":"#1"},{"id":"20140921/1226fba49769c08a2402bd8adefc47c27935fab3c05cede67a756cb2462b084d.jpeg","rs":[{"rs":"360","w":360,"h":356},{"rs":"230","w":230,"h":227}],"mid":"#10"},{"id":"20140921/b508dbeb8eb76059e1b066a4ab859c0b9f881b6da2894258c71b2dd0aaf1cade.jpeg","rs":[{"rs":"360","w":360,"h":355},{"rs":"230","w":230,"h":227}],"mid":"#11"},{"id":"20140921/f86f13fc5bc008f4471172ce03f95a518cf009fe9ddf8a03d99e520f3500a26b.jpeg","rs":[{"rs":"360","w":360,"h":357},{"rs":"230","w":230,"h":228}],"mid":"#12"},{"id":"20140921/c9fd65387fb9d3945d4b5bc06d3d43fcf8d86251ec0204fab5926c0f8deab05f.jpeg","rs":[{"rs":"360","w":360,"h":360},{"rs":"230","w":230,"h":230}],"mid":"#13"},{"id":"20140921/89a4027cb49e329361d94e03664ec046a477bb2b455aa543c8a5cffe29dc94ae.jpeg","rs":[{"rs":"360","w":360,"h":360},{"rs":"230","w":230,"h":230}],"mid":"#14"},{"id":"20140921/1a68a899cf613f0a2245fa1a61aba36b6f44cb5716a3bd95518b2d67a35bc9d6.jpeg","rs":[{"rs":"360","w":360,"h":466},{"rs":"230","w":230,"h":297}],"mid":"#2"},{"id":"20140921/cc92d2ccd191ba704108595fc0990a7e589d82be6c09053568c0a6fc1896c395.jpeg","rs":[{"rs":"360","w":360,"h":360},{"rs":"230","w":230,"h":230}],"mid":"#3"},{"id":"20140921/49a88894e6c74942f389c9a1447beab65c50879938d3f1fff358bf58b690e77f.jpeg","rs":[{"rs":"360","w":360,"h":358},{"rs":"230","w":230,"h":228}],"mid":"#4"},{"id":"20140921/a1888c28810e68a583b212d7e316014ff4abe9f2fbe887bf5a8f98135aeb15e5.jpeg","rs":[{"rs":"360","w":360,"h":361},{"rs":"230","w":230,"h":231}],"mid":"#5"},{"id":"20140921/0ed9b897522bf08641dc7d2f35a31961a951bb5e6a8c4d27b57d04c8ff8fd789.jpeg","rs":[{"rs":"360","w":360,"h":360},{"rs":"230","w":230,"h":230}],"mid":"#6"},{"id":"20140921/43e7985fca4f1768519d93764a0864e5e88400dd3c1b5230458e9136dcc5d929.jpeg","rs":[{"rs":"360","w":360,"h":357},{"rs":"230","w":230,"h":228}],"mid":"#7"},{"id":"20140921/8611ae9a1712bab85913bc7202ab0ac92c02a4c29cbe10a051e461d8d5174bc8.jpeg","rs":[{"rs":"360","w":360,"h":330},{"rs":"230","w":230,"h":211}],"mid":"#8"},{"id":"20140921/1a33e91d42e600ff2e34abc112514ed2ba5fef5c0db34b578dc7ef29d2fd3cd2.jpeg","rs":[{"rs":"360","w":360,"h":466},{"rs":"230","w":230,"h":297}],"mid":"#9"}],"adv":null,"in_img":0}},{"meta":{"id":"MR:29191946","u":"http://www.appledaily.com.tw/mobile/rnewsarticle/artid/473819/issueid/20140922/","src":"台灣蘋果日報","tl":"帝國之子對幹老闆　控A錢自爆險尋短","ts":1411316940015,"pid":1119,"tp":1,"a":"台灣蘋果日報","img":[{"id":"20140921/origin/b0413c4d8356dbc14e58fa185facb8f82b75806668612d5056f5ea6e49274802--.jpeg","w":376,"h":446,"c":"文俊英身為韓團「ZE：A帝國之子」的隊長，昨在推特大吐對所屬公司的不滿。翻攝韓國me2day微博","mid":"#1"}],"v":null,"th":{"id":"20140921/47fdec66ef574068e09ef957ee2806f3076048bf05690a0876923df022f9d429.jpeg","rs":["360","230"],"mid":"#1"},"tids":["1398121"],"fg":0,"int":null,"bth":[{"id":"20140921/47fdec66ef574068e09ef957ee2806f3076048bf05690a0876923df022f9d429.jpeg","rs":[{"rs":"360","w":360,"h":427},{"rs":"230","w":230,"h":272}],"mid":"#1"}],"adv":null,"in_img":0}}],"top_items":[],"bl":[]},"ntf":[{"command":"DEL_TOPIC","ts":null,"args":[{"n":"Headline News", "tid":1319108},{"n":"Technology & Science Highlights", "tid":1320486},{"n":"蘋果日報", "tid":1398121}]}]}');
		  	blinkfeed.fillItem(defaultData.res.items,0);
		    return defaultData;
		  })
	}
}
