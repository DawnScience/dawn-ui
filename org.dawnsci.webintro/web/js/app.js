function javaReady(){
	updateData();
}

function updateData(){
	
	
	$( "#title span" ).first().show( "slow", function showNext() {
		$( this ).next( "#title span" ).show( "slow", showNext );
	});
	$("#data-here").hide(400);
	data = $.parseJSON(java.getIntroJSON());
		
	data.pages.forEach(function(entry){
//		var source   = $("#entry-template").html();
//		var template = Handlebars.compile(source);
//		$("#entry-template").replaceWith();
//		console.log(template);
		
		$("#tabList").append(
		"<li role='presentation'><a href='#"+entry.page_id+"' aria-controls='"+entry.page_id+"' role='tab' data-toggle='tab'>"+entry.name+"</a></li>");
		$("#tabContent").append(
		"<div role='tabpanel' class='tab-pane fade' id='"+entry.page_id+"'>"+marked(entry.content, {gfm:true,breaks:true})+"</div>");
		
		$("#tabContent #"+entry.page_id).append('<ul></ul>');
		entry.actions.forEach(function(action){
			$( "<li/>", {
				text: action.name,
				data: {
					action_id: action.id
				},
				click: function() {
					java.runAction($(this).data('action_id'));
				},
				
			}).appendTo( "#tabContent #"+entry.page_id+" ul" );

		});
		
		$('#tabList a:first').tab('show');

	});
//	$("#data-here").html(java.getIntroJSON());
	$("#data-here").slideDown(3000);
}