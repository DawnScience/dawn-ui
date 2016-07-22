console.log("Loaded init js");

demoData = {"pages":[{"id":"org.dawnsci.webintro.content.introPage1","page_id":"welcome","name":"Intro Page 1","content":"**Hello, this is some test content**\n~~Now with some new lines~~\n","image":"platform:/plugin/org.dawnsci.webintro/null","items":[{"id":"org.dawnsci.webintro.introLink1","name":"A Link to a website","image":"platform:/plugin/org.dawnsci.webintro/null","description":"This is an introLink contribution, it should take you to the ~~dawn~~ website","isContent":false,"isAction":false,"isLink":true,"href":"http://dawnsci.org"},{"id":"org.dawnsci.webintro.introContent1","name":"This is a content contribution","image":"platform:/plugin/org.dawnsci.webintro/null","description":"A description here","isContent":true,"isAction":false,"isLink":false,"content":"This is some content for the new page\n\n~~Some strikethrough~~\n\n##A title##\n"},{"id":"org.dawnsci.spectrum.ui.introAction1","name":"Spectrum Perspective Launch","image":"platform:/plugin/org.dawnsci.spectrum.ui/icons/Trace400.png","description":"This is an action contribution Designed for viewing line graphs Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est labor","isContent":false,"isAction":true,"isLink":false}]},{"id":"org.dawnsci.webintro.content.introPage2","page_id":"welcome2","name":"Intro Page 2","content":"Hello, this is some test content for the second page\n\nNow with some new lines\n","image":"platform:/plugin/org.dawnsci.webintro/null","items":[]}]};
timer = setTimeout(function(){ console.log("Manually starting since no java"); updateData(demoData);  }, 1000);

function javaReady(){
	updateData($.parseJSON(java.getIntroJSON()));
	clearTimeout(timer);
	console.log("Running java ready");
//	$('html').html(java.getIntroJSON());
}

function updateData(data){

	data.pages.forEach(function(entry){
		entry["content_rendered"] = marked(entry.content, {gfm:true,breaks:true});

		entry.items.forEach(function(item){
			item["description_rendered"] = marked(item.description, {gfm:true,breaks:true});
			if(item.content){
				item["content_rendered"] = marked(item.content, {gfm:true,breaks:true});
			}
		});
	});

	Handlebars.registerHelper('losedots', function(s) { return s.replace(/\./g,'_'); });

	var source   = $("#tpl-tab_headings").html();
	var template = Handlebars.compile(source);
	$("#placeholder-tab_headings").replaceWith(template(data));

	var source2   = $("#tpl-tab_content").html();
	var template2 = Handlebars.compile(source2);
	$("#placeholder-tab_content").replaceWith(template2(data));

	$('ul.tabs').tabs();
	$('.content-trigger').leanModal();
	$('.action-trigger').click(function(){
		java.runAction($(this).data('action_id'));
	});
	$('.link-trigger').click(function(){
		java.openLink($(this).data('href'));
	});

	Materialize.showStaggeredList('.collection');
	Materialize.fadeInImage('#dawn_logo');
}