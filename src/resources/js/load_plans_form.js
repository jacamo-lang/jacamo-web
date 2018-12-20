

	
if (typeof Promise !== "undefined") {
	function synonyms(cm, option) {
		return new Promise(function(accept) {
			setTimeout(function() {
				var cursor = cm.getCursor(), line = cm.getLine(cursor.line);
				var start = cursor.ch, end = cursor.ch;
				while (start && /\\w/.test(line.charAt(start - 1)))
					--start;
				while (end < line.length && /\\w/.test(line.charAt(end)))
					++end;
				var word = line.slice(start, end).toLowerCase();
				return accept({
					list : parent.window.suggestions,
					from : CodeMirror.Pos(cursor.line, start),
					to : CodeMirror.Pos(cursor.line, end)
				});
				return accept(null);
			}, 100)
		})
	}

	var editor = CodeMirror.fromTextArea(document.getElementById("code2"), {
		lineNumbers : true,
		extraKeys : { "Ctrl-Space" : "autocomplete"},
		mode : {name : "erlang", globalVars : true},
	    matchBrackets: true,
	    continueComments: "Enter",
		/*hintOptions : {hint : synonyms},*/
		/*mode : {name : "javascript", globalVars : true},*/
	});
}
