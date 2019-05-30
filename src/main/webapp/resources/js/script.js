var leftPane = document.getElementById('left-pane');
var rightPane = document.getElementById('right-pane');
var paneSep = document.getElementById('panes-separator');

// The script below constrains the target to move horizontally between a left and a right virtual boundaries.
// - the left limit is positioned at 10% of the screen width
// - the right limit is positioned at 90% of the screen width
var leftLimit = 25;
var rightLimit = 50;

paneSep.sdrag(function (el, pageX, startX, pageY, startY, fix) {
    // console.log(pageX + ' ' + startX + ' ' + pageY + ' ' + startY);

    fix.skipX = true;

    if (pageX < window.innerWidth * leftLimit / 100) {
        pageX = window.innerWidth * leftLimit / 100;
        fix.pageX = pageX;
    }
    if (pageX > window.innerWidth * rightLimit / 100) {
        pageX = window.innerWidth * rightLimit / 100;
        fix.pageX = pageX;
    }

    var cur = pageX / window.innerWidth * 100;
    if (cur < 0) {
        cur = 0;
    }
    if (cur > window.innerWidth) {
        cur = window.innerWidth;
    }

    var right = (100-cur-2);
    leftPane.style.width = cur + '%';
    rightPane.style.width = right + '%';
}, null, 'horizontal');

// Функция сохраняет позицию scroll
function saveScrollPos() {
    document.getElementById('tabView:scrollPos').value = document.getElementById('tabView:dataGrid_scrollState').value;
}

// Функция возвращет сохранненный scroll
function autoScroll() {
    var scrollPos = document.getElementById('tabView:scrollPos').value;
    var arr = scrollPos.split(',');
    // console.log('load position ' + scrollPos);
    var attr = $('#tabView\\:dataGrid .ui-datatable-scrollable-body');
    attr.scrollTop(arr[1]);
    attr.scrollLeft(arr[0]);
}

// Функция для отрисовки yandex карт
function initMap(pAddress) {
    var myMap = new ymaps.Map('map', {
        // При инициализации карты обязательно нужно указать
        // её центр и коэффициент масштабирования.
        center: [55.76, 37.64], // Москва
        zoom: 18
    });
    var myGeocoder = ymaps.geocode(pAddress, {results: 1});
    myGeocoder.then(
        function (res) {
            myMap.setCenter(res.geoObjects.get(0).geometry.getCoordinates(), 18);
            myMap.geoObjects.add(res.geoObjects)
        }
    )
}

// Выставляем нужный id и устанавливаем высоту
function loadTableId() {
    var item = document.getElementById("tabView:dataGrid_scrollableTbody");
    if (item != null) {
        document.getElementById("tabView:dataGrid_scrollableTbody").parentElement.setAttribute("id", "dataGridForSelect");
        document.getElementById("tabView:dataGrid_scrollableThead").children[0].setAttribute("style", "height: 40px");
    }
}

jQuery(window).on('load', function() {
    loadTableId();
});

//bind every click on any td in the table
jQuery(document).delegate("#dataGridForSelect td", "click", function (event) {
    //get index of clicked row
    var columnNumber = jQuery(this).index();
    // console.log(columnNumber);
    var attr = jQuery("#tabView\\:mySelectedColumnId");
    //set value in the inputText
    attr.val(columnNumber);
    //this will trigger the ajax listener
    attr.change();
    jsCall();
});