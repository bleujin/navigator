'use strict';

var dndApp = angular.module('dndApp', []);

dndApp.directive('dndDraggable', function() {
    return {
        // A = attribute, E = Element, C = Class and M = HTML Comment
        restrict: 'A',
        link: function(scope, element, attrs, controller) {
            var helper = attrs.helper;

            if (helper == undefined) {
                helper = 'original';
            }

            element.draggable({
                revert: false,
                helper: 'clone',
                iframeFix: true,
                drag: function(event, ui) {
                    $(this).addClass("drag-active");
                    $(this).closest(element).addClass("drag-active");
                },
                stop: function(event, ui) {
                    $(this).removeClass("drag-active").closest(element).removeClass("drag-active");
                    var droppedEl = angular.element(ui.droppable);
                    //console.log(droppedEl);
                }
            });
        }
    }
});

dndApp.directive('dndDroppableIframe', function($compile) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {

            if (element.prop('tagName') !== 'IFRAME') {
                return;
            }

            element.ready(function() {
                var ifrDoc = element.contents();
                var ifrBody = ifrDoc.find('body');

//                var dropDiv = ifrBody.find("#droppable-div");

                ifrBody.attr('dnd-droppable', '');

                $compile(ifrBody)(scope);
            });

            element.load(function() {
                var ifrDoc = element.contents();
                var ifrBody = ifrDoc.find('body');

                ifrBody.attr('dnd-droppable', '');

                $compile(ifrBody)(scope);
            });
        }
    }
});

dndApp.directive('dndDroppable', function($compile) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs, controller) {
            var greedy = attrs.greedy;
            if (greedy == undefined) {
                greedy = true;
            }
            //console.log("inside-droppable");
            //console.log(element);
            element.droppable({
                activeClass: "drop-active",
                greedy: greedy,
                hoverClass: "drop-hover",
                iframeFix: true,
                tolerance: "intersect",

                drop: function(event, ui) {

                    var rawHtml = angular.element(ui.draggable).data('raw');
                    var draggedEl = angular.element(ui.draggable); //.parent();
                    var droppedEl = angular.element(this);

                    if (rawHtml == undefined) {
                        rawHtml = $(draggedEl).clone();
                    }

                    var el = $(rawHtml);

//                    $compile(rawHtml)(scope).appendTo(droppedEl);
                    el.removeAttr('dnd-draggable');
                    el.removeAttr('dnd-droppable');
                    el.appendTo(droppedEl);

                    if (angular.element(ui.draggable).data('helper') == undefined) {
                        $(draggedEl).remove();
                    }

                    window.onIFrameDrop(el.clone());
                }
            });
        }
    }
});