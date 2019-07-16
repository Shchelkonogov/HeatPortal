// Функции вызываемые из xhtml

// Функция сохраняет позицию scroll
function saveScrollPos() {
    document.getElementById('tabView:scrollPos').value = document.getElementById('tabView:dataGrid_scrollState').value;
}

// Функция возвращет сохранненный scroll
function setScrollPos() {
    var scrollPos = document.getElementById('tabView:scrollPos').value;
    var arr = scrollPos.split(',');
    var attr = $('#tabView\\:dataGrid .ui-datatable-scrollable-body');
    attr.scrollTop(arr[1]);
    attr.scrollLeft(arr[0]);
}

// Функция для отрисовки Yandex карт
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





// Функции вызываемые из js и xhtml

// Функция добавляет к таблице обработчики событий
function setTableEvents() {
    document.getElementById("tabView:dataGrid").addEventListener("mousedown", tableMouseDown);
    document.getElementById("tabView:dataGrid").addEventListener("mouseup", tableMouseUp);
    document.getElementById("tabView:dataGrid").addEventListener("mousemove", tableMouseMove);
}





// Глобальные переменные

var count;
var isMouseDown = false;
var xPos;





// Функции jQuery

// Функция инициализации колонок таблицы срабатывает при загрузки страницы
if (document.getElementById("tabView:dataGrid") != null) {
    jQuery(window).on('load', function() {
        setTableEvents();
        if (document.getElementById("tabView:dataGrid").offsetWidth < 2000) {
            count = "min";
        } else {
            count = "max";
        }
        initTable([{name: 'count', value: count}]);
    });
}

// Функция для обработки изменения размера окна
jQuery(window).resize(function() {
    checkChangeSize();
});

// Функция события нажатия на td в таблице для определение номера колонки
jQuery(document).delegate("#tabView\\:dataGrid_data td", "click", function() {
    // get index of clicked row
    var columnNumber = jQuery(this).index() - 7;
    if (columnNumber >= 0) {
        // console.log(columnNumber);
        var attr = jQuery("#tabView\\:mySelectedColumnId");
        // set value in the inputText
        attr.val(columnNumber);
        // this will trigger the ajax listener
        attr.change();
        tableColumnClick();
    }
});





// Функции вызываемые из js

// Функция проверки изменения размера таблицы
function checkChangeSize() {
    if (document.getElementById("tabView:dataGrid") != null) {
        // console.log(document.getElementById("tabView:dataGrid").offsetWidth);
        if (count === "max" && document.getElementById("tabView:dataGrid").offsetWidth < 2000) {
            count = "min";
            // console.log("change min");
            initTable([{name: 'count', value: count}]);
        }
        if (count === "min" && document.getElementById("tabView:dataGrid").offsetWidth > 2000) {
            count = "max";
            // console.log("change max");
            initTable([{name: 'count', value: count}]);
        }
    }
}

// Функция обработки mouseDown
function tableMouseDown(event) {
    isMouseDown = true;
    xPos = event.pageX;
    // console.log('tableMouseDown ' + isMouseDown);
}


// Функция обработки mouseUp
function tableMouseUp() {
    isMouseDown = false;
    // console.log('tableMouseUp ' + isMouseDown);
}

// Функция обработки mouseMove
function tableMouseMove(event) {
    if (isMouseDown) {
        // console.log('tableMouseMove ' + event.pageX + ' ' + (event.pageX - xPos));
        if (event.pageX - xPos > 50) {
            // console.log('updateData next');
            xPos = event.pageX;
            moveTableData([{name: 'direction', value: 'right'}]);
        }

        if (event.pageX - xPos < -50) {
            // console.log('updateData before');
            xPos = event.pageX;
            moveTableData([{name: 'direction', value: 'left'}]);
        }
    }
}

// Функция для работы с кнопкой скрыть навигацию
function collapseButtonClick() {
    if (document.getElementById('left-pane').style.display === 'none') {
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

    checkChangeSize();
}