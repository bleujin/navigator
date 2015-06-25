if (!Array.prototype.has) {
	Array.prototype.has = function (node) {
		if (!node['id']) return false;

		for (var i = 0, max = this.length; i < max; i++) {
			var id = this[i]['id'];
			if (id && id === node.id) {
				return true;
			}
		}
		return false;
	};
}

if (!Array.prototype.merge) {
	Array.prototype.merge = function (nodes) {

	};
}

(function ($, window) {
	function Stage(stageId) {
		var _stage = $(stageId), _stageSelector = '#' + stageId, instance, _self = this, windows, codeEditor, _nodes;

		var w = window.innerWidth, h = window.innerHeight;

		var newNode = null, selectedNodeEl = null, selectedConnection = null;

		var clicked = false, prevX, prevY;

		$(window).keydown(function (e) {
			if (selectedNodeEl !== null) {
				if (e.keyCode === 46) { // Del Key
					alert('Delete node : ' + selectedNodeEl.attr('id'));
				}
			}
		});

		$('#main').dblclick(function (e) {
			var randomId = Math.floor(Math.random() * (10000) + 1), nodeObj = {
				id      : '' + randomId,
				name    : 'node_' + randomId,
				path    : '/node_' + randomId,
				children: []
			}, clickPosX = e.offsetX, clickPosY = e.offsetY;

			var nodeEl = appendEl(nodeObj, clickPosX, clickPosY);


			newNode = nodeObj;

			_nodes[_self.toUUID(newNode.id)] = newNode;
			_unsavedNodes[newNode.id] = newNode;

		}).mousedown(function (e) {
			e.preventDefault();

			prevX = e.clientX;
			prevY = e.clientY;
			clicked = true;
		}).mouseup(function (e) {
			clicked = false;
		}).mousemove(function (e) {
			if (e.ctrlKey && clicked) {
				e.preventDefault();

				var moveXTo = $('#main').scrollLeft() + (prevX - e.clientX), moveYTo = $('#main').scrollTop() + (prevY - e.clientY);

				$('#main').scrollLeft(moveXTo);
				$('#main').scrollTop(moveYTo);

				prevX = e.clientX;
				prevY = e.clientY;
			}
		}).mouseleave(function (e) {
			clicked = false;
		});


		this.locator = {
			centerLocator: function () {
				var _x = $(_stageSelector).innerWidth() / 2, _y = $(_stageSelector).innerHeight() / 2;

				return {
					x: _x,
					y: _y
				};
			},
			circleLocator: function (parentX, parentY, numOfChildren) {
				var increase = Math.PI * 2 / numOfChildren, angle = 0, radius = 200, positionMap = [];

				for (var i = 0; i < numOfChildren; i++) {
					var left = parentX + radius * Math.cos(Math.PI * i / numOfChildren * 2 - Math.PI / 2),
                        top = parentY + radius * Math.sin(Math.PI * i / numOfChildren * 2 - Math.PI / 2);

					positionMap.push({
						x: left,
						y: top
					});
					angle += increase;
				}

				return positionMap;

			}
		};

		this.nodes = function () {
			return _nodes;
		};

		this.getNode = function (nodeId) {
			return _nodes[nodeId];
		};

		this.toUUID = function (id) {
			// /airkjh/child1/grandchild1 ==> airkjh_child1_grandchld1
			var uuid = id;
			if (id === 'root') {
				return uuid;
			}

			if (uuid.indexOf('/') === 0) {
				uuid = uuid.substr(1);
			}

			return uuid.split('/').join('___');
		};

		this.idToPath = function (id) {

			if (id === 'root') {
				return '/';
			}

			return '/' + id.split('___').join('/');
		};

		var _unsavedNodes = {};

		this.isUnsavedNode = function(nodeId) {
			return _unsavedNodes[nodeId] !== undefined;
		};

		var appendEl = function (node, left, top) {

			node['uuid'] = _self.toUUID(node.id);

			if (!_nodes[node.uuid]) {
				_nodes[node.uuid] = node;
			}

			var el = $('<div>', {
				id: node.uuid
			}).appendTo('#stage');

			// append div to stage
			var name = (node.properties && node.properties.name) || node.name || 'Untitled', expander = node.uuid + '_expander', nodeEl = $(el), hasChildren = node.children.length > 0, expanderIcon = hasChildren ? 'glyphicon-plus' : 'glyphicon-minus', expanderEl = '<div id="' + expander + '"class="expand"><span class="glyphicon ' + expanderIcon + '"></span></div>';

			nodeEl.addClass('w');
			nodeEl.css('position', 'absolute').css('left', left).css('top', top);
			nodeEl.append('<label style="cursor: pointer;" id="' + node.uuid + '_label"><strong>' + name + '</strong></label>' + expanderEl + '<div class="ep"></div>');
			nodeEl.attr('data-display', 'properties');
			nodeEl.contextMenu('nodeMenu', {
				bindings: {
					'preview': function (nodeEl) {

                        var path = _self.idToPath(nodeEl.id);

                        window.open('http://localhost:9000/preview' + path + '.html');
					},
					'refresh': function (nodeEl) {
						alert('Refreshing ' + nodeEl.id);
					}
				}
			});

			var tooltipContent = 'Path: <b>' + node.path + '</b><br/>Number of Children: ' + node.children.length;

			$('#' + node.uuid + '_label').hovercard({
				detailsHTML: tooltipContent,
				width      : 300,
				delay      : 1000
			});

			// add event handler
			// nodeEl.dblclick(nodeDblclickHandler);
			nodeEl.click(function (e) {
				var el = $(this);

				if (selectedNodeEl !== null && selectedNodeEl.attr('id') !== el.attr('id')) {
					selectedNodeEl.removeClass('selected');
				}

				if (selectedNodeEl !== null) {
					selectedNodeEl.removeClass('selected');
					selectedNodeEl = null;
				}

				el.addClass('selected');
				selectedNodeEl = el;
			});


			nodeEl.data('showChildren', false);

			// set visibility expander
			if (!hasChildren) {
				$('#' + expander).hide();
			}

			$('#' + expander).click(function (e) {
				var childrenShown = nodeEl.data('showChildren'), expanderId = '#' + node.uuid + ' span', expanderEl = $(expanderId);

				// toggle expander icon
				if (hasChildren) {
					if (!childrenShown) {
						expanderEl.removeClass('glyphicon-plus').addClass('glyphicon-minus');
					} else {
						expanderEl.removeClass('glyphicon-minus').addClass('glyphicon-plus');
					}

					nodeEl.data('showChildren', !childrenShown);
				}

				toggle(node, node.children);

				e.stopPropagation();
			});

			decorate(nodeEl);

			return nodeEl;
		};

		var decorate = function (el) {

			instance.makeSource(el, {
				filter        : ".ep",
				anchor        : "Continuous",
				connector     : ["StateMachine", {
					curviness: 0
				}],
				connectorStyle: {
					strokeStyle : "#5c96bc",
					lineWidth   : 2,
					outlineColor: "transparent",
					outlineWidth: 4
				},
				maxConnections: -1
			});

			instance.makeTarget(el, {
				dropOptions: {
					hoverClass: 'dragHover'
				},
				anchor     : 'Continuous'
			});

			instance.draggable(el, {
				containment: _stageSelector,
				start      : function (e) {
					e.stopPropagation(); // stop container drag
				},
				drag       : function (e) {
					if (e.ctrlKey) {
						e.preventDefault();
					}
				},
				stop       : function (e) {
					var target = e.target;
					$('#' + target.id).attr('dragged', true);
				}
			});
		};

		var hideRecursive = function (nodes) {
			for (var i = 0, max = nodes.length; i < max; i++) {
				var node = nodes[i], nodeEl = $('#' + node.uuid);

				nodeEl.hide();

				instance.hide(node.uuid, true);

				if (node.children.length > 0) {
					hideRecursive(node.children);
				}
			}
		};

		var toggle = function (parent, children) {

			var parentEl = $('#' + parent.uuid),
				parentPos = parentEl.position(),
				x = parentPos.left,
				y = parentPos.top,
				childPos = _self.locator.circleLocator(x, y, children.length);

			for (var i = 0, max = children.length; i < max; i++) {
				var child = children[i], childEl = $('#' + child.uuid);

				if (childEl.length > 0) { // element once rendered already
					if (childEl.is(':hidden')) {
						// relocate relative to parent node's new position
						if (childEl.attr('dragged')) {
							// once node is dragged, keep position of node
							instance.recalculateOffsets(childEl);
						}

						childEl.show();
						instance.show(child.uuid, true);
					} else {
						hideRecursive(child.children);

						childEl.hide();
						instance.hide(child.uuid, true);
					}
				} else {
					childEl = appendEl(child, childPos[i].x, childPos[i].y);

					_self.connect(child.uuid, parent.uuid);
				}
			}
		};

		var deselectConnection = function (conn) {
			conn.setPaintStyle({
				strokeStyle: '#5c96bc'
			});
			selectedConnection = null;
		};

		var selectConnection = function (conn) {
			conn.setPaintStyle({
				strokeStyle: '#FF0000'
			});
			selectedConnection = conn;
		};

		var bindEventHandler = function (connection) {

			connection.bind('dblclick',function (conn, e) {
				e.stopPropagation();

				var connEditor = $('#connEditorPanel');
				connEditor.modal('show');

				$('#connEditorPanel .action_button').on('click', function () {
					var label = $('#connName').val();

					conn.getOverlay('label').setLabel(label || Stage.childRelationName);

					var n = noty({
						layout : 'topCenter',
						text   : 'Connection name updated successfully',
						type   : 'success',
						timeout: 2000
					});

					connEditor.modal('hide');
				});

			}).bind('click', function (conn, e) {

				if (selectedConnection === null) {

					selectConnection(conn);

				} else if (selectedConnection !== null && selectedConnection === conn) {

					deselectConnection(selectedConnection);

				} else if (selectedConnection !== null && selectedConnection !== conn) {

					deselectConnection(selectedConnection);
					selectConnection(conn);

				} else {
					console.log('else');
					console.log(selectedConnection);
					console.log(conn);
				}
			});
		};

		this.connect = function (src, target, options) {
			instance.connect({
				source: src,
				target: target
			});
		};

		var flatten = function (nodes) {

			var result = {};

			function recurs(nodes) {
				for (var i = 0; i < nodes.length; i++) {
					var node = nodes[i], _children = [];

					if (node.children) {
						_children = node.children;
					}
					var uuid = _self.toUUID(node.id);
					result[uuid] = node;
					recurs(_children);
				}
			}

			recurs(nodes);

			return result;

		};

		this.updateConnection = function(connection, label) {
			bindEventHandler(connection);

			connection.getOverlay('label').setLabel(label);

			$('#' + connection.targetId + '_expander').show();
		};

		var appendAsChild = function(parentNodeId, tempId, props) {
			var node = _nodes[tempId],
				parentNode = _nodes[parentNodeId];

			// update as new connection
			node.id = props.id;
			node.name = props.name;
			node.uuid = props.uuid;
			node.path = props.path;

			_nodes[node.id] = node;

			parentNode.children.push(node);
			parentNode.childrenCount += 1;

			delete _unsavedNodes[tempId];
			delete _nodes[tempId];
		};

		this.detachConnection = function(connection) {
			instance.detach(connection, {forceDetach: true});
		};

		this.connectAsChild = function(connection) {
			var parentNodePath = _self.idToPath(connection.targetId);

			new NodeNameDialog(this).callback(function(nodeName) {
				var parentNodeUUID = connection.targetId === 'root' ? '' : connection.targetId + '_',
					parentNodeId = connection.targetId,
					currentId = connection.sourceId,
					newNodeId = parentNodeUUID + nodeName,
					parentNode = _nodes[parentNodeId],
					newPath = (parentNode.path + '/' + nodeName).replace('//', '/');

				$('#' + currentId + ' label').html('<strong>' + nodeName + '</strong>');

				instance.setId(currentId, newNodeId, false);

				var newProp = {
					id: newPath,
					name: nodeName,
					uuid: _self.toUUID(newPath),
					path: newPath
				};

				console.log(newProp);

				appendAsChild(parentNodeId, currentId, newProp);

				noty({
					layout : 'topCenter',
					text   : 'Node has been created',
					type   : 'success',
					timeout: 2000
				});

			}).show(connection.sourceId, parentNodePath, connection);
		};

		this.init = function (nodes) {
			_nodes = flatten(nodes);

			instance = jsPlumb.getInstance({
				Endpoint          : ["Blank", {

				}],
				HoverPaintStyle   : {
					strokeStyle: '#1e8151',
					lineWidth  : 2
				},
				DragOptions       : {
					cursor: 'pointer',
					zIndex: 2000
				},
				ConnectionOverlays: [
					["Arrow", {
						location: 1,
						id      : 'arrow',
						length  : 12,
						foldback: 0.8
					}],
					["Label", {
						label   : "include",
						id      : "label",
						cssClass: "aLabel"
					}]
				],
				Container         : stageId
			});

			var initPos = _self.locator.centerLocator();
			var rootEl = appendEl(nodes[0], initPos.x, initPos.y);
			var refDialog = new ReferenceDialog(this);

			var isConnectingToUnsavedNode = function(connection) {
				return _self.isUnsavedNode(connection.targetId);
			};

			var isConnectingAsChild = function(connection) {
				return _self.isUnsavedNode(connection.sourceId) && !_self.isUnsavedNode(connection.targetId);
			};

			var isExpandingConnection = function(connection) {
				return !_self.isUnsavedNode(connection.sourceId) && !_self.isUnsavedNode(connection.targetId);
			}

			instance.bind('connection', function (connInfo, originalEvent) {
				var connection = connInfo.connection;

				if(isConnectingToUnsavedNode(connection)) {

					NC.Progress.fail('Invalid connection');
					_self.detachConnection(connection);

				} else if(isConnectingAsChild(connection)) {
					console.log('targetId = ' + connection.targetId);
					_self.connectAsChild(connection);

				} else if(isExpandingConnection(connection)) {
					// source, target nodes are already saved...
					_self.updateConnection(connection, Stage.childRelationName);

				} else {
					// this reference, so show popup
					refDialog.show(connection);
				}

			});

			instance.doWhileSuspended(function () {
				instance.makeSource(rootEl, {
					filter        : ".ep", // only supported by jquery
					anchor        : "Continuous",
					connector     : ["StateMachine", {
						curviness: 0
					}],
					connectorStyle: {
						strokeStyle : "#5c96bc",
						lineWidth   : 2,
						outlineColor: "transparent",
						outlineWidth: 4
					},
					maxConnections: -1
				});

				instance.makeTarget(rootEl, {
					dropOptions: {
						hoverClass: 'dragHover'
					},
					anchor     : 'Continuous'
				});
			});

			var _propertyPanel = new PropertyPanel();

			this.propertyPanel = function () {
				return _propertyPanel;
			}

			$('#main').animate({
				scrollTop : $('#root').position().top - $('#main').height() / 2 + $('#root').height() / 2,
				scrollLeft: $('#root').position().left - $('#main').width() / 2 + $('#root').width() / 2
			}, 'slow');
		};

		Stage.childRelationName = 'include';
	}

	window.Stage = Stage;

})(jQuery, window);