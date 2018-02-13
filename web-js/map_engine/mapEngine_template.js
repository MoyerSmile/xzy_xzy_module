var XZYMap = function(view,opts){
	this.view = view;
	this.init(opts);
};
//地图上添加的组件类型
XZYMap.CONTROL_TYPE = {
		PAN:1,
		ZOOM:2,
		OVERVIEW:3,
		LOCATE:4,
		SCALE:5,
		MAP_TYPE:6,
		PANORAMA:7,
		TRAFFIC:8
};
//可设置的地图行为类型
XZYMap.ACTION_TYPE = {
		DRAG:1,  //拖动
		WHEEL_ZOOM:2,
		DOUBLE_CLICK_ZOOM:3,
		KEY_BOARD:4,
		INTERTIAL_DRAG:5, //惯性拖动
		CONTINOUS_ZOOM:6, //持续性缩放
		PINCH_ZOOM:7,
		AUTO_RESIZE:8
};
//鼠标类型
XZYMap.CURSOR_TYPE = {
		ARROW:1,
		HAND:2,
		MOVE:3
};
//图层类型
XZYMap.OVERLAY_TYPE = {
		MARK_TYPE:1,
		SHAPE_TYPE:2,
		POINT_COLLECTION:3
};
//点类型
XZYMap.POINT_TYPE = {
		STAR:1,
		CYCLE:2,
		SQUARE:3,
		RHOMBUS:4,
		WATERDROP:5
};
//初始化真实的地图引擎对象
XZYMap.prototype.init = function(opts){
	
};
//添加不同的地图界面控制组件
XZYMap.prototype.addControl = function(controlType,opts){
	
};
XZYMap.prototype.removeControl = function(controlType){
	
};
//添加一些默认支持的行为到地图引擎上
XZYMap.prototype.enableAction = function(actionType,isEnable){
	
};
//为不同的操作状态设定鼠标图标
XZYMap.prototype.setDefaultCursor = function(cursor){
	
};

XZYMap.prototype.setDraggingCursor = function(cursor){
	
};
//设定期望的地图最小和最大缩放级别
XZYMap.prototype.setMinZoom = function(zoom){
	
};
XZYMap.prototype.setMaxZoom = function(zoom){
	
};
//得到当前视野的经纬度范围
XZYMap.prototype.getBounds = function(){
	
};
//得到中心点经纬度
XZYMap.prototype.getCenter = function(){
	
};
//得到视野的像素大小
XZYMap.prototype.getSize = function(){
	
};
//得到当前的缩放级别
XZYMap.prototype.getZoom = function(){
	
};
//设置中心点和缩放级别
XZYMap.prototype.centerAndZoom = function(center,zoom){
	
};
//设置中心点
XZYMap.prototype.setCenter = function(center){
	
};
//设置缩放级别
XZYMap.prototype.setZoom = function(zoom){
	
};
//放大一级
XZYMap.prototype.zoomIn = function(){
	
};
//缩小一级
XZYMap.prototype.zoomOut = function(){
	
};
//重新中心和缩放到初始
XZYMap.prototype.reset = function(){
	
};
//得到两点之间的距离
XZYMap.prototype.getDistance = function(start,end){
	
};
//平滑移动到
XZYMap.prototype.panTo = function(center){
	
};
//平滑移动开多少像素
XZYMap.prototype.panBy = function(pixelX,pixelY){
	
};
//设定右键菜单
XZYMap.prototype.setContextMenu = function(menuArr){
	
};
//打开一个信息窗口
XZYMap.prototype.openInfoWindow = function(view,opts){
	
};
//关闭信息窗口
XZYMap.prototype.closeInfoWindow = function(){
	
};
//得到当前打开的信息窗口
XZYMap.prototype.getInfoWindow = function(){
	
};
//经纬度到界面像素的转换
XZYMap.prototype.pointToPixel = function(lolaPoint){
	
};
//界面像素到经纬度的转换
XZYMap.prototype.pixelToPoint = function(pixel){
	
};
//添加一个图层
XZYMap.prototype.addOverlay = function(name,type,opts){
	
};
//移除一个图层
XZYMap.prototype.removeOverlay = function(name){
	
};
//清理所有的图层
XZYMap.prototype.clearOverlays = function(){
	
};
//得到所有的图层
XZYMap.prototype.getOverlays = function(){
	
};
//地标的添加和移除
XZYMap.prototype.addMarker = function(overlayName,marker,opts){
	
};
XZYMap.prototype.addMarkers = function(overlayName,markers,opts){
	
};
XZYMap.prototype.removeMarker = function(overlayName,marker){
	
};
XZYMap.prototype.clearMarkers = function(overlayName){
	
};
//线路的添加和移除
XZYMap.prototype.addPolyline = function(overlayName,polyline,opts){
	
};
XZYMap.prototype.removePolyline  = function(overlayName,polyline){
	
};
XZYMap.prototype.clearPolylines = function(overlayName){
	
};
//多边形的添加和移除
XZYMap.prototype.addPolygon = function(overlayName,polygon,opts){
	
};
XZYMap.prototype.removePolygon  = function(overlayName,polygon){
	
};
XZYMap.prototype.clearPolygons = function(overlayName){
	
};
//椭圆的添加和移除
XZYMap.prototype.addEllipse = function(overlayName,ellipse,opts){
	
};
XZYMap.prototype.removeEllipse  = function(overlayName,ellipse){
	
};
XZYMap.prototype.clearEllipses = function(overlayName){
	
};
//矩形的添加和移除
XZYMap.prototype.addRectangle = function(overlayName,rect,opts){
	
};
XZYMap.prototype.removeRectangle  = function(overlayName,rect){
	
};
XZYMap.prototype.clearRectangles = function(overlayName){
	
};



