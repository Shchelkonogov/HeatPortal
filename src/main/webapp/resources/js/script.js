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
    var myGeoCoder = ymaps.geocode(pAddress, {results: 1});
    myGeoCoder.then(
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

function collapseButtonClick() {
    if (document.getElementById('left-pane').style.display == 'none') {
        document.getElementById('left-pane').style.display = '';
        document.getElementById('collapseButtonIcon').style.display = '';
        document.getElementById('collapseButton').className = 'collapseButton';
        document.getElementById('collapseButton').title = 'Скрыть панель навигации';
    } else {
        document.getElementById('left-pane').style.display = 'none';
        document.getElementById('collapseButtonIcon').style.display = 'none';
        document.getElementById('collapseButton').className = 'collapseButtonHidden';
        document.getElementById('collapseButton').title = 'Показать панель навигации';
    }
}