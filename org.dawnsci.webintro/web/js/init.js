console.log("Loaded init js");

demoData = {"pages":[{"id":"org.dawnsci.webintro.content.introPage1","page_id":"welcome","name":"Demo","content":"**Hello, this is some test content**\n~~Now with some new lines~~\n","items":[{"id":"org.dawnsci.webintro.introCategory1","name":"Test Category","image":"","description":"Test category, hopefully this will animate nicely","isContent":false,"isAction":false,"isLink":false,"isCategory":true,"items":[{"id":"org.dawnsci.webintro.introAction2","name":"Open Cheat Sheet","image":"","description":"This should hopefully open a cheat sheet","isContent":false,"isAction":true,"isLink":false,"isCategory":false},{"id":"org.dawnsci.webintro.content.about-dawn","name":"About DAWN","image":"","description":"","isContent":true,"isAction":false,"isLink":false,"isCategory":false,"content":"![alt text](platform:/plugin/org.dawnsci.webintro/content/img/about_dawn_image.png \"DAWN Logo\")\n\nDAWN is an open source software built on the Eclipse/RCP platform in order to scale to address a wide range of applications and to benefit from the workbench and advanced plugin system implemented in Eclipse. The main scientific domains which DAWN targets are powder diffraction, macromolecular crystallography, and tomography. Any scientific domain which needs visualisation, python and workflows can profit from DAWN.\n\nDAWN is a Java-based open source project based on the Eclipse Rich Client Platform with its advanced plugin architecture. This gives DAWN the possibility to be extended with functionality in virtually any direction.\n\nThe main institutes developing DAWN are the [Diamond Light Source](http://www.diamond.ac.uk/), and the [European Synchrotron Radiation Facility](http://www.esrf.eu/).\n"}]},{"id":"org.dawnsci.webintro.introLink1","name":"A Link to a website","image":"","description":"This is an introLink contribution, it should take you to the **dawn** website","isContent":false,"isAction":false,"isLink":true,"isCategory":false,"href":"http://dawnsci.org"},{"id":"org.dawnsci.webintro.introContent1","name":"This is a content contribution","image":"","description":"A description here","isContent":true,"isAction":false,"isLink":false,"isCategory":false,"content":"This is some content for the new page\n\n~~Some strikethrough~~\n\n##A title##\n"},{"id":"org.dawnsci.spectrum.ui.introAction1","name":"Spectrum Perspective Launch","image":"platform:/plugin/org.dawnsci.spectrum.ui/icons/Trace400.png","description":"This is an action contribution Designed for viewing line graphs Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est labor","isContent":false,"isAction":true,"isLink":false,"isCategory":false}]},{"id":"org.dawnsci.webintro.pages.overview","page_id":"overview","name":"Overview","content":"Welcome to **DAWN**, the **D**ata **A**nalysis **W**orkbe**N**ch, you may find the links below useful\n","items":[{"id":"org.dawnsci.webintro.content.licence","name":"License","image":"","description":"Link to the description of the EPL license used in DAWN","isContent":false,"isAction":false,"isLink":true,"isCategory":false,"href":"http://www.eclipse.org/legal/epl-v10.html"}]},{"id":"org.dawnsci.webintro.content.other","page_id":"org.dawnsci.webintro.content.other","name":"Other","content":"These items were not assigned to a page:","items":[{"id":"uk.ac.diamond.scisoft.beamlineexplorer.rcp.introAction2","name":"Beamline Data","image":"","description":"Test wizard action","isContent":false,"isAction":true,"isLink":false,"isCategory":false}]}]};
timer = setTimeout(function(){ console.log("Manually starting since no java"); updateData(demoData);  }, 1000);

function javaReady(){
	//$('html').html(java.getIntroJSON());

	updateData($.parseJSON(java.getIntroJSON()));
	clearTimeout(timer);
	console.log("Running java ready");
}

function renderItemMarkdown(item){
	console.log("Rendering Description for "+item);
	item["description_rendered"] = marked(item.description, {gfm:true,breaks:true});
	if(item.content){
		console.log("Rendering Content");
		item["content_rendered"] = marked(item.content, {gfm:true,breaks:true});
	}else if(item.isCategory && item.items){
		item.items.forEach(renderItemMarkdown);
	}
}

// Adapted from Materialize.showStaggeredList function
// Now only animates top level of lists
function animateList(selectorOrEl){
	var element;
	if (typeof(selectorOrEl) === 'string') {
		element = $(selectorOrEl);
	} else if (typeof(selectorOrEl) === 'object') {
		element = selectorOrEl;
	} else {
		return;
	}
	var time = 0;
	element.find('>li').velocity(
			{ translateX: "-100px"},
			{ duration: 0 });

	element.find('>li').each(function() {
		$(this).velocity(
				{ opacity: "1", translateX: "0"},
				{ duration: 800, delay: time, easing: [60, 10] });
		time += 120;
	});
}

function updateData(data){

	// Render all of the markdown to HTML
	data.pages.forEach(function(entry){
		entry["content_rendered"] = marked(entry.content, {gfm:true,breaks:true});
		entry.items.forEach(renderItemMarkdown);
	});

	// Register template helpers
	Handlebars.registerHelper('losedots', function(s) { return s.replace(/\./g,'_'); });
	Handlebars.registerPartial('itemRender', $("#tpl-item").html());
	
	// Render the templates and insert into the DOM
	var source   = $("#tpl-tab_headings").html();
	var template = Handlebars.compile(source);
	$("#placeholder-tab_headings").replaceWith(template(data));

	var source2   = $("#tpl-tab_content").html();
	var template2 = Handlebars.compile(source2);
	$("#placeholder-tab_content").replaceWith(template2(data));
	
	var source3   = $("#tpl-modal_data").html();
	var template3 = Handlebars.compile(source3);
	$("#placeholder-modal_data").replaceWith(template3(data));

	// Setup Category Items
	$('.popout-item').click(function(){
		$(this).toggleClass("active"); // Open and close the category on click
	});
	$('ul.main-list>li').click(function(){
		$('.popout-item').not($(this)).removeClass('active'); // If another button is clicked, close the category
	});
	
	$('ul.tabs').tabs(); // Render the tabs
	var tabWidth = 100 / $('ul.tabs li.tab').size();
	$('ul.tabs li.tab').width(tabWidth+"%");
	
	$('.content-trigger').leanModal(); // Setup modal boxes for content items
	$('.action-trigger').click(function(){
		java.runAction($(this).data('action_id')); // Run an action
	});
	$('.link-trigger').click(function(){
		java.openLink($(this).data('href')); // Open a link in the system browser
	});
	
	// Make all external links use the java external link API
	$("a[href^='http://'],a[href^='https://']").click(function(event){
		event.preventDefault();
        event.stopPropagation();
        java.openLink(this.href);
	});

	// Do some animations
	animateList('.collection.main-list');
	Materialize.fadeInImage('#dawn_logo');
}