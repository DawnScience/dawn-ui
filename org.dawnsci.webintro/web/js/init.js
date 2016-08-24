console.log("Loaded init js");

demoData = {"pages":[{"id":"org.dawnsci.webintro.content.pages.science","page_id":"science","name":"Science","content":"Welcome to **DAWN**, the **D**ata **A**nalysis **W**orkbe**N**ch, you may find the links below useful\n","items":[{"id":"org.dawnsci.webintro.category.tomography","name":"Tomography","image":"","description":"","isContent":false,"isAction":false,"isLink":false,"isCategory":true,"items":[]},{"id":"org.dawnsci.webintro.category.powderdiffraction","name":"Powder Diffraction","image":"","description":"","isContent":false,"isAction":false,"isLink":false,"isCategory":true,"items":[{"id":"org.dawnsci.webintro.introLink2","name":"Test Link 2","image":"","description":"","isContent":false,"isAction":false,"isLink":true,"isCategory":false,"href":"http://"},{"id":"org.dawnsci.webintro.introLink1","name":"Test Link 1","image":"","description":"","isContent":false,"isAction":false,"isLink":true,"isCategory":false,"href":"http://example.org"}]},{"id":"org.dawnsci.webintro.category.NCD","name":"NCD (Non-Crystalline Diffraction)","image":"","description":"","isContent":false,"isAction":false,"isLink":false,"isCategory":true,"items":[]},{"id":"org.dawnsci.webintro.category.ARPES","name":"ARPES","image":"","description":"(angle-resolved photoemission spectroscopy)","isContent":false,"isAction":false,"isLink":false,"isCategory":true,"items":[]}]},{"id":"org.dawnsci.webintro.content.pages.dawn","page_id":"dawn","name":"DAWN","content":"Welcome to **DAWN**, the **D**ata **A**nalysis **W**orkbe**N**ch, you may find the links below useful\n","items":[{"id":"org.dawnsci.webintro.category.scripting","name":"Scripting","image":"","description":"","isContent":false,"isAction":false,"isLink":false,"isCategory":true,"items":[]},{"id":"org.dawnsci.webintro.category.dawnindawn","name":"DAWN Development Environment","image":"","description":"","isContent":false,"isAction":false,"isLink":false,"isCategory":true,"items":[]},{"id":"org.dawnsci.webintro.category.processes","name":"Processes","image":"","description":"","isContent":false,"isAction":false,"isLink":false,"isCategory":true,"items":[]}]},{"id":"org.dawnsci.webintro.content.pages.help","page_id":"help","name":"Help","content":"Welcome to **DAWN**, the **D**ata **A**nalysis **W**orkbe**N**ch, you may find the links below useful\n","items":[{"id":"org.dawnsci.webintro.link.youtube","name":"DAWN YouTube Channel","image":"","description":"Contains tutorial videos","isContent":false,"isAction":false,"isLink":true,"isCategory":false,"href":"http://youtube.com/DAWNScience"},{"id":"org.dawnsci.webintro.link.dawnwebsite","name":"DAWN Website","image":"","description":"","isContent":false,"isAction":false,"isLink":true,"isCategory":false,"href":"http://dawnsci.org"},{"id":"org.dawnsci.webintro.content.what-is-dawn","name":"What Is DAWN","image":"","description":"","isContent":true,"isAction":false,"isLink":false,"isCategory":false,"content":"![alt text](platform:/plugin/org.dawnsci.webintro/content/img/about_dawn_image.png \"DAWN Logo\")\n\nDAWN is an open source software built on the Eclipse/RCP platform in order to scale to address a wide range of applications and to benefit from the workbench and advanced plugin system implemented in Eclipse. The main scientific domains which DAWN targets are powder diffraction, macromolecular crystallography, and tomography. Any scientific domain which needs visualisation, python and workflows can profit from DAWN.\n\nDAWN is a Java-based open source project based on the Eclipse Rich Client Platform with its advanced plugin architecture. This gives DAWN the possibility to be extended with functionality in virtually any direction.\n\nThe main institutes developing DAWN are the [Diamond Light Source](http://www.diamond.ac.uk/), and the [European Synchrotron Radiation Facility](http://www.esrf.eu/).\n"},{"id":"org.dawnsci.webintro.link.licence","name":"License","image":"","description":"Link to the description of the EPL license used in DAWN","isContent":false,"isAction":false,"isLink":true,"isCategory":false,"href":"http://www.eclipse.org/legal/epl-v10.html"},{"id":"uk.ac.diamond.scisoft.feedback","name":"Leave Feedback","image":"","description":"Open a feedback form","isContent":true,"isAction":false,"isLink":false,"isCategory":false,"content":""}]}]};
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

	$('.version-number').html(java.getVersion());
	
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

	var source4   = $("#tpl-feedback-form").html();
	var template4 = Handlebars.compile(source4);
	$("#modal-uk_ac_diamond_scisoft_feedback div.modal-content").append(template4());
	
	// Setup Category Items
	$('.popout-item').click(function(){
		$(this).toggleClass("active"); // Open and close the category on click
		if( $(this).find(".popout").offset().top < 0 ){ // if it's off the top of the screen
			$(this).find(".popout").css('transform', 'translateY(-300px)'); // Move it to a sensible location
		} 
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
	
	// Allow the feedback form to send data to the eclipse feedback plugin
	$("#send_external").click(function(e){
		email = $('#feedback-form').find('input[name="email"]').val();
		subject = $('#feedback-form').find('input[name="subject"]').val();
		message = $('#feedback-form').find('textarea[name="message"]').val();

		feedbackUtil.openFeedback(email, subject, message);
	});
	
	// Do some animations
	animateList('.collection.main-list');
	Materialize.fadeInImage('#dawn_logo');
}