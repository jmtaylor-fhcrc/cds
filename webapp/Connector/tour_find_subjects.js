var tour_find_subjects = {
    title:       'Find-subjects',
    description: 'A tour for the "Find subjects" section.',
    id:          'tour-find-subjects',
    onClose:     function(){
        hopscotch.endTour();
    },
    onError:     function(){
        hopscotch.endTour();
    },
    steps:
    [ 
        {
            target:            'div.nav-label:nth-child(3)',
            placement:         'left',
            title:             'Find subjects',
            content:           'Click here to find subjects with common characteristics - products, assays, studies, ect.',
            yOffset:           -17,
            onNext:            function() {
                document.querySelector('div.nav-label:nth-child(3) > span:nth-child(2)').click();
                var checkExist = setInterval(
                    function() {
                        if (document.querySelector('div.row:nth-child(1)') !== null){
                            window.location = 'cds-app.view?#summary';
                            setTimeout( function() {document.querySelector("div.row:nth-child(1)").classList.add("row-autohover")}, 0);
                            setTimeout( function() {document.querySelector("div.row:nth-child(1)").classList.remove("row-autohover")}, 1000);
                            setTimeout( function() {document.querySelector("div.row:nth-child(2)").classList.add("row-autohover")}, 500);
                            setTimeout( function() {document.querySelector("div.row:nth-child(2)").classList.remove("row-autohover")}, 1500);
                            setTimeout( function() {document.querySelector("div.row:nth-child(3)").classList.add("row-autohover")}, 1000);
                            setTimeout( function() {document.querySelector("div.row:nth-child(3)").classList.remove("row-autohover")}, 2000);
                            setTimeout( function() {document.querySelector("div.row:nth-child(4)").classList.add("row-autohover")}, 1500);
                            setTimeout( function() {document.querySelector("div.row:nth-child(4)").classList.remove("row-autohover")}, 2500);
                            window.hopscotch.startTour(window.hopscotch.getCurrTour(), window.hopscotch.getCurrStepNum());
                            clearInterval(checkExist);
                        }
                    }, 100);
            },
            multipage: true
        },{
            target:            'div.row:nth-child(1)',
            placement:         'top',
            arrowOffset:       'center',
            xOffset:           70,
            content:           'These sections below allow you to search and group parameters of interest'
        },{
            target:            'div.row:nth-child(2)',
            placement:         'top',
            arrowOffset:       'center',
            xOffset:           70,
            content:           'Clicking the "Products" row will let you find the products for which there is data available in DataSpace',
            onNext:            function(){
                document.querySelector("div.row:nth-child(2)").click();
                var checkExist = setInterval(
                    function(){
                        if (document.querySelector('div[id="sae-hierarchy-dropdown"]') !== null && document.querySelector('div[id*="singleaxisview"]') !== null) {
                            window.hopscotch.startTour(window.hopscotch.getCurrTour(), window.hopscotch.getCurrStepNum());
                            clearInterval(checkExist);
                        };
                    }, 100);
            },
	    multipage: true
        },{
            target:            'div[id*="singleaxisview"]',
            placement:         'top',
            arrowOffset:       'left',
            xOffset:           70,
            content:           'This column has product names.',
            onNext:            function(){
                var checkExist = setInterval(
                    function(){
                        if(document.querySelector('div.bar:nth-child(12)') !== null){
                            document.querySelector('div.bar:nth-child(12)').classList.add('index-selected', 'inactive');
                            document.querySelector('div.bar:nth-child(15)').classList.add('index-selected', 'inactive');
                            document.querySelector('div.bar:nth-child(20)').classList.add('index-selected', 'inactive');
                            window.hopscotch.startTour(window.hopscotch.getCurrTour(), window.hopscotch.getCurrStepNum());
                            clearInterval(checkExist);
                        };
                    }, 100);
            },
        },{
            target:            'div.bar:nth-child(12)',
            placement:         'top',
            arrowOffset:       'left',
            xOffset:           70,
            content:           'Find a product of interest. Teal indicates overlap with other products.',
            onNext:            function(){
                document.querySelector('div.bar:nth-child(12)').click();
                document.querySelector('div.bar:nth-child(12)').classList.remove('index-selected', 'inactive');
                document.querySelector('div.bar:nth-child(15)').classList.remove('index-selected', 'inactive');
                document.querySelector('div.bar:nth-child(20)').classList.remove('index-selected', 'inactive');
                window.hopscotch.startTour(window.hopscotch.getCurrTour(), window.hopscotch.getCurrStepNum()); 
            }
        },{
            target:      'h2[class*="filterheader-text"]',
            placement:   'left',
            arrowOffset: 'center',
            xOffset:     0,
	    yOffset:     -65,
            content:     'Clicking the row adds the item to the Active Filter. The Active Filter holds your selection, using the section for other parts of the app, like plotting and downloading.',
        },{
	    target:      'a[class="x-btn x-unselectable x-btn-toolbar x-box-item x-toolbar-item x-btn-rounded-inverted-accent-toolbar-small x-noicon x-btn-noicon x-btn-rounded-inverted-accent-toolbar-small-noicon"]',
	    placement:   'bottom',
	    arrowOffset: '250',
	    xOffset:     -250,
	    content:     'Clicking the Filter button applies the filter to the page.',
	    onNext:      function(){
		document.getElementsByClassName('x-btn x-unselectable x-btn-toolbar x-box-item x-toolbar-item x-btn-rounded-inverted-accent-toolbar-small x-noicon x-btn-noicon x-btn-rounded-inverted-accent-toolbar-small-noicon')[0].click();		
	    }
	},{
	    target:      'ul[class="detailstatus"]',
	    placement:   'bottom',
	    arrowOffset: '250',
	    xOffset:     -100,
	    yOffset:     10,
	    content:     'The total number of subjects have been reduced and a summary of those subjects are seen here.'
	},{
            target:      'h2[class*="filterheader-text"]',
            placement:   'left',
            arrowOffset: 'center',
            xOffset:     0,
	    yOffset:     -45,
	    content:     'You can save your filter or clear it by clicking "clear" or "save" here.',
	    onNext:      function(){
		document.querySelector('#button-1037-btnIconEl').click();
	    }
	},{
            target:    'div.nav-label:nth-child(1) > span:nth-child(2)',
            placement: 'left',
            content:   'Click home when you are done browsing learn about.',
            yOffset:   -17,
            onNext:    function() {
                document.querySelector('div.nav-label:nth-child(1) > span:nth-child(2)').click();
                var checkExist = setInterval(
                    function() {
                        if (document.querySelector('.started-section-title').offsetParent !== null) {
                            clearInterval(checkExist);
                            window.hopscotch.startTour(window.hopscotch.getCurrTour(), window.hopscotch.getCurrStepNum());
                        }
                    }, 100);
            }
        },{
            target:      '.tour-section-title',
            placement:   'top',
	    arrowOffset: 'center',
            content:     'Click here for more tours!',
	    xOffset:     'center'
        }
    ]
};
