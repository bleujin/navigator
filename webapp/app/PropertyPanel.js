(function($, window) {
	function PropertyPanel() {

		var box, codeEditor;

		var _self = this;

		box = $('.portBox');

		$(document).on("dblclick", "[data-display]", function (t) {
			$(this).showPortBox(t);
		});

		codeEditor = new jsoneditor.JSONEditor(document.getElementById('codeEditor'), {
			mode: 'code'
		});

		$('#codeEditor_save').click(function (e) {
			NC.Progress.show('Saving changes...');

			var nodePath = _self.getNodePath(), body = codeEditor.get();

			if(!nodePath) {
				alert("No nodepath");
				return;
			}

			if (!body) {
				body = {};
			}

			$.post(NC.REST.propertyLet + nodePath, {body: JSON.stringify(body)},function (response) {
				NC.Progress.success('Node has updated', 1000);
			}).fail(function (e) {
				NC.Progress.fail("Updating node property has failed : " + '[' + e.status + ']' + e.statusText, 1000);
			});
		});

		$('#codeEditor_close').click(function (e) {
			box.trigger('portBox:close');
		});

		this.getBox = function () {
			return box;
		};

		this.codeEditor = function () {
			return codeEditor;
		};

		this.param = {};

		this.setNodePath = function(nodePath) {
			this.param.nodePath = nodePath;
		}

		this.getNodePath = function() {
			return this.param.nodePath;
		}

	}

	window.PropertyPanel = PropertyPanel;

})(jQuery, window);


