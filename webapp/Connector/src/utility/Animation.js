/*
 * Copyright (c) 2014 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

Ext.define('Animation', {
    singleton: true,

    // Copy a source object and float it to a destination object
    floatTo: function(node, sourceQuery, targetQueries, animateElementType, animateElementClass, completionCallback, callbackScope, callbackParams) {
        var target = null;
        var targetTop = false, found = false, box;

        // Determine which animation end point is best
        for (var i = 0; i < targetQueries.length && !found; ++i) {
            target = Ext.DomQuery.select(targetQueries[i]);
            if (Ext.isArray(target) && target.length > 0) {
                var el = Ext.get(target[0]);
                if (el.isVisible()) {
                    // use the selection panel
                    targetTop = true;
                    found = true;
                    box = el.getBox();
                }
            }
        }

        // Visibile doesn't necessarily work...
        if (found && (box.x == 0 || box.y == 0)) {
            found = false;
        }

        if (found) {
            if (Ext.isElement(node)) {
                // Convert DOM element to Ext element
                node = Ext.get(node);
            }
            var child = node.query(sourceQuery);
            child = Ext.get(child[0]);
            var cbox = child.getBox();

            // Create DOM Element replicate
            var dom = document.createElement(animateElementType);
            dom.innerHTML = child.dom.innerHTML;
            dom.setAttribute('class', animateElementClass);
            dom.setAttribute('style', 'position: absolute; width: ' + (child.getTextWidth()+20) + 'px; left: ' + cbox[0] + 'px; top: ' + cbox[1] + 'px;');
            // Append to Body
            var xdom = Ext.get(dom);
//            xdom.setXY(child.getXY());
            xdom.appendTo(Ext.getBody());

            var y = box.y + 30;
            if (!targetTop) {
                y += box.height;
            }

            xdom.animate({
                to : {
                    x: box.x,
                    y: y,
                    opacity: 0.2
                },
                duration: 1000, // Issue: 15220
                listeners : {
                    afteranimate : function() {
                        Ext.removeNode(xdom.dom);
                    }
                }
            });
        }

        if (completionCallback) {
            var task = new Ext.util.DelayedTask(completionCallback, callbackScope, callbackParams);
            task.delay(found ? 500 : 0);
        }
    }
});