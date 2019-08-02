var tour_learn_about = {
    title:       'Learn-about',
    description: 'A tour for the "Learn about" section.',
    id:          'tour-learn-about',
    onClose:     function(){
        hopscotch.endTour();
    },
    onError:     function(){
        hopscotch.endTour();
    },
    steps:
    [
        {
            target:            'div.nav-label:nth-child(2) > span:nth-child(2)',
            placement:         'left',
            title:             'Learn about',
            content:           'Click here to browse information regarding studies, products, assays, publications, and other topics.',
            yOffset:           -17,
            onNext:            function() {
                document.querySelector('div.nav-label:nth-child(2) > span:nth-child(2)').click();
                var checkExist = setInterval(
                    function() {
                        if (document.querySelector('div[class*="learn-dim-selector"]') !== null && document.querySelector('tr[data-recordindex="0"]') !== null) {
                            window.location = 'cds-app.view?#learn/learn/Study';
                            clearInterval(checkExist);
                            window.hopscotch.startTour(window.hopscotch.getCurrTour(), window.hopscotch.getCurrStepNum());
                        }
                    }, 100);
            },
            multipage: true
        },{
            target:            'h1.lhdv:nth-child(3)',
            placement:         'bottom',
            arrowOffset:       'center',
            xOffset:           -90,
            content:           'Click any of the tabs here to browse a topic.',
            onNext:            function(){
                window.location = 'cds-app.view?#learn/learn/Study%20Product';
		document.querySelector("h1.lhdv:nth-child(3)").click();
                var checkExist = setInterval(
                    function() {
                        function triggerEvent(el, type){
                            if ('createEvent' in document) {
                                // modern browsers, IE9+
                                var e = document.createEvent('HTMLEvents');
                                e.initEvent(type, false, true);
                                el.dispatchEvent(e);
                            } else {
                                // IE 8
                                var e = document.createEventObject();
                                e.eventType = type;
                                el.fireEvent('on'+e.eventType, e);
                            }
                        }
                        if ( document.querySelector('h2[id="2F54E10mim"]') !== null ) {
                            clearInterval(checkExist);
                            window.hopscotch.startTour(window.hopscotch.getCurrTour(), window.hopscotch.getCurrStepNum());
                            var inbox = document.querySelector('input');
                            inbox.value = "ALVAC";
                            triggerEvent(inbox, "change");
                        }
                    }, 100);
            },
            multipage: true
        },{
            target:            'input[placeholder="Search products"]',
            placement:         'left',
            arrowOffset:       'center',
            yOffset:           -35,
            smoothScroll:      false,
            content:           'Enter text here to search the catalog. Search both names and descriptions.',
            onNext:            function() {
                var checkExist = setInterval(
                    function() {
                        if( document.querySelector('h2[id="ALVACvCP15"]').parentElement.parentElement.parentElement.parentElement.getAttribute("data-recordindex") == "0" ){
                            clearInterval(checkExist);
                            window.hopscotch.startTour(window.hopscotch.getCurrTour(), window.hopscotch.getCurrStepNum());
                        }
                    }, 100);
            }
        },{
            target:            'h2[id="ALVACvCP15"]',
            placement:         'top',
            content:           'Click on a record.',
            yOffset:           0,
            onNext:            function() {
                document.querySelector('h2[id="ALVACvCP15"]').click();          
                var checkExist = setInterval(
                    function() {
                        if (document.querySelector('div[id*="app-module-productheader"]') !== null) {
                            clearInterval(checkExist);
                            window.hopscotch.startTour(window.hopscotch.getCurrTour(), window.hopscotch.getCurrStepNum());
                        }
                    }, 100);
            },
            multipage: true
        },{
            target:            'div[id*="app-module-productheader"]',
            placement:         'top',
            content:           'Review product information.',
            yOffset:           -17
        },{
            target:            '.iarrow',
            placement:         'right',
            arrowOffset:       'top',
            content:           'Click this back arrow to return to the previous Learn about page.',
            onNext:            function() {
		window.location = 'cds-app.view?#learn/learn/Study%20Product'
		var checkExist = setInterval(
                    function() {
                        if (document.querySelector('div.nav-label:nth-child(1)').offsetParent !== null) {
                            clearInterval(checkExist);
                            window.hopscotch.startTour(window.hopscotch.getCurrTour(), window.hopscotch.getCurrStepNum());
                        }
                    }, 100)
            },
	    multipage: true
        },{
            target:            'div.nav-label:nth-child(1)',
            placement:         'left',
            content:           'Click home when you are done browsing learn about.',
            yOffset:           -17,
            onNext:            function() {
                function triggerEvent(el, type){
                    if ('createEvent' in document) {
                        // modern browsers, IE9+
                        var e = document.createEvent('HTMLEvents');
                        e.initEvent(type, false, true);
                        el.dispatchEvent(e);
                    } else {
                        // IE 8
                        var e = document.createEventObject();
                        e.eventType = type;
                        el.fireEvent('on'+e.eventType, e);
                    }
                }
                var inbox = document.querySelector('input');
                inbox.value = "";
                triggerEvent(inbox, "change");
                document.querySelector('div.nav-label:nth-child(1)').click();
                var checkExist = setInterval(
                    function() {
                        if (document.querySelector('#expanded-intro-div').offsetParent !== null) {
                            clearInterval(checkExist);
                            window.hopscotch.startTour(window.hopscotch.getCurrTour(), window.hopscotch.getCurrStepNum());
                        }
                    }, 100)
            }
        },{
            target:            'h3[class*="tour-section-title"]',
            placement:         'top',
	    arrowOffset:       'center',
            content:           'Click here for more tours!',
	    xOffset:           'center'
        }
	
    ]
};
