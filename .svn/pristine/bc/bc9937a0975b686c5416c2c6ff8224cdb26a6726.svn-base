var XZYMap = function(view,opts){
	this.view = view;
	this.controlArr = [];
	this.overlayMapping = {};
	this.init(opts);
};
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
XZYMap.ACTION_TYPE = {
		DRAG:1,
		WHEEL_ZOOM:2,
		DOUBLE_CLICK_ZOOM:3,
		KEY_BOARD:4,
		INTERTIAL_DRAG:5,
		CONTINOUS_ZOOM:6,
		PINCH_ZOOM:7,
		AUTO_RESIZE:8
};
XZYMap.CURSOR_TYPE = {
		ARROW:1,
		HAND:2,
		MOVE:3
};
XZYMap.OVERLAY_TYPE = {
		MARK_TYPE:1,
		SHAPE_TYPE:2,
		POINT_COLLECTION:3
};
XZYMap.POINT_TYPE = {
		STAR:1,
		CYCLE:2,
		SQUARE:3,
		RHOMBUS:4,
		WATERDROP:5
};

XZYMap.prototype.init = function(opts){
	this.map = new BMap.Map(this.view,{enableMapClick:false});

	var center = {lng:121.521253,lat:31.308611};
	var zoom = 18;
	if(opts && opts.center){
		center = opts.center;
	}
	if(opts && opts.zoom){
		zoom = opts.zoom;
	}
	this.centerAndZoom(center, zoom);
	
	if(opts && opts.control){
		for(var i=0;i<opts.control.length;i++){
			this.addControl(opts.control[i]);
		}
	}else{
		this.addControl(XZYMap.CONTROL_TYPE.PAN);
		this.addControl(XZYMap.CONTROL_TYPE.ZOOM);
		this.addControl(XZYMap.CONTROL_TYPE.SCALE,{offset:{x:100,y:40}});
		this.addControl(XZYMap.CONTROL_TYPE.MAP_TYPE);
	}

	this.enableAction(XZYMap.ACTION_TYPE.DRAG,true);
	this.enableAction(XZYMap.ACTION_TYPE.WHEEL_ZOOM,true);
	this.enableAction(XZYMap.ACTION_TYPE.AUTO_RESIZE,true);
};
XZYMap.prototype.addControl = function(controlType,opts){
	var control = null;
	if(controlType == XZYMap.CONTROL_TYPE.PAN){
		if(this._existControl(BMap.NavigationControl)){
			return ;
		}
		control = new BMap.NavigationControl({anchor: this._getAnchor(opts?opts.pos:1),offset:this._getOffset(opts)});
	}else if(controlType == XZYMap.CONTROL_TYPE.ZOOM){
		if(this._existControl(BMap.NavigationControl)){
			return ;
		}
		control = new BMap.NavigationControl({anchor: this._getAnchor(opts?opts.pos:1),offset:this._getOffset(opts)});
	}else if(controlType == XZYMap.CONTROL_TYPE.OVERVIEW){
		if(this._existControl(BMap.OverviewMapControl)){
			return ;
		}
		control = new BMap.OverviewMapControl({isOpen:true,anchor: this._getAnchor(opts?opts.pos:9),offset:this._getOffset(opts)});
	}else if(controlType == XZYMap.CONTROL_TYPE.LOCATE){
		if(this._existControl(BMap.GeolocationControl)){
			return ;
		}
		control = new BMap.GeolocationControl({anchor: this._getAnchor(opts?opts.pos:7),offset:this._getOffset(opts)});
	}else if(controlType == XZYMap.CONTROL_TYPE.SCALE){
		if(this._existControl(BMap.ScaleControl)){
			return ;
		}
		control = new BMap.ScaleControl({anchor: this._getAnchor(opts?opts.pos:7),offset:this._getOffset(opts)});
	}else if(controlType == XZYMap.CONTROL_TYPE.MAP_TYPE){
		if(this._existControl(BMap.MapTypeControl)){
			return ;
		}
		control = new BMap.MapTypeControl({anchor: this._getAnchor(opts?opts.pos:3),offset:this._getOffset(opts)});
	}else if(controlType == XZYMap.CONTROL_TYPE.PANORAMA){
		if(this._existControl(BMap.PanoramaCoverageLayer)){
			return ;
		}
		this.map.addTileLayer(new BMap.PanoramaCoverageLayer());
		control = new BMap.PanoramaControl({anchor: this._getAnchor(opts?opts.pos:3),offset:this._getOffset(opts)});
	}else if(controlType == XZYMap.CONTROL_TYPE.TRAFFIC){
		if(typeof BMapLib.TrafficControl == undefined){
			return ;
		}
		if(this._existControl(BMapLib.TrafficControl)){
			return ;
		}
		control = new BMapLib.TrafficControl();
	}

	this.controlArr[this.controlArr.length] = control;
	this.map.addControl(control);
	if(controlType == XZYMap.CONTROL_TYPE.TRAFFIC){
		control.setAnchor(this._getAnchor(opts?opts.pos:9)); 
		control.setOffset(this._getOffset(opts));
	}
};
XZYMap.prototype._getOffset = function(opts){
	if(opts == null || opts.offset == null){
		return new BMap.Size(20,20);
	}
	return new BMap.Size(opts.offset.x, opts.offset.y);
};
XZYMap.prototype._getAnchor = function(pos){
	if(pos == 1 || pos == 2 || pos == 4 || pos == 5){
		return BMAP_ANCHOR_TOP_LEFT;
	}else if(pos == 3 || pos == 6){
		return BMAP_ANCHOR_TOP_RIGHT;
	}else if(pos == 9){
		return BMAP_ANCHOR_BOTTOM_RIGHT;
	}else{
		return BMAP_ANCHOR_BOTTOM_LEFT;
	}
};
XZYMap.prototype._existControl = function(cls){
	if(!cls){
		return false;
	}
	for(var i=0;i<this.controlArr.length;i++){
		if(this.controlArr[i] instanceof cls){
			return true;
		}
	}
	return false;
};
XZYMap.prototype._removeControl = function(cls){
	for(var i=this.controlArr.length-1;i>=0;i--){
		if(this.controlArr[i] instanceof cls){
			this.map.removeControl(this.controlArr[i]);
			this.controlArr.splice(i,1);
		}
	}
};
XZYMap.prototype.removeControl = function(controlType){
	if(controlType == XZYMap.CONTROL_TYPE.PAN){
		this._removeControl(BMap.NavigationControl);
	}else if(controlType == XZYMap.CONTROL_TYPE.ZOOM){
		this._removeControl(BMap.NavigationControl);
	}else if(controlType == XZYMap.CONTROL_TYPE.OVERVIEW){
		this._removeControl(BMap.OverviewMapControl);
	}else if(controlType == XZYMap.CONTROL_TYPE.LOCATE){
		this._removeControl(BMap.GeolocationControl);
	}else if(controlType == XZYMap.CONTROL_TYPE.SCALE){
		this._removeControl(BMap.ScaleControl);
	}else if(controlType == XZYMap.CONTROL_TYPE.MAP_TYPE){
		this._removeControl(BMap.MapTypeControl);
	}else if(controlType == XZYMap.CONTROL_TYPE.PANORAMA){
		this._removeControl(BMap.PanoramaControl);
	}else if(controlType == XZYMap.CONTROL_TYPE.TRAFFIC){
		this._removeControl(BMapLib.TrafficControl);
	}
};
XZYMap.prototype.enableAction = function(actionType,isEnable){
	if(actionType == XZYMap.ACTION_TYPE.DRAG){
		if(isEnable){
			this.map.enableDragging();
		}else{
			this.map.disableDragging();
		}
	}else if(actionType == XZYMap.ACTION_TYPE.WHEEL_ZOOM){
		this.map.enableScrollWheelZoom(isEnable);
	}else if(actionType == XZYMap.ACTION_TYPE.DOUBLE_CLICK_ZOOM){
		
	}else if(actionType == XZYMap.ACTION_TYPE.KEY_BOARD){
		
	}else if(actionType == XZYMap.ACTION_TYPE.INTERTIAL_DRAG){
		if(isEnable){
			this.map.enableInertialDragging();
		}else{
			this.map.disableInertialDragging();
		}
	}else if(actionType == XZYMap.ACTION_TYPE.CONTINOUS_ZOOM){
		
	}else if(actionType == XZYMap.ACTION_TYPE.AUTO_RESIZE){
		
	}
};

XZYMap.prototype.setDefaultCursor = function(cursor){
	
};

XZYMap.prototype.setDraggingCursor = function(cursor){
	
};

XZYMap.prototype.setMinZoom = function(zoom){
	this.map.setMinZoom(zoom);
};
XZYMap.prototype.setMaxZoom = function(zoom){
	this.map.setMaxZoom(zoom);
};
XZYMap.prototype.getBounds = function(){
	var bd = this.map.getBounds();
	
	var s = bd.getSouthWest();
	var b = bd.getNorthEast();
	
	return {slng:s.lng,slat:s.lat,width:b.lng-s.lng,height:b.lat-s.lat};
};
XZYMap.prototype.getCenter = function(){
	return this.map.getCenter(); 
};
XZYMap.prototype.getSize = function(){
	return this.map.getSize();
};
XZYMap.prototype.getZoom = function(){
	return this.map.getZoom();
};
XZYMap.prototype.centerAndZoom = function(center,zoom){
	this.map.centerAndZoom(new BMap.Point(center.lng, center.lat),zoom);
};
XZYMap.prototype.setCenter = function(center){
	this.map.setCenter(new BMap.Point(center.lng, center.lat));
};
XZYMap.prototype.setZoom = function(zoom){
	this.map.setZoom(zoom);
};
XZYMap.prototype.zoomIn = function(){
	this.map.zoomIn();
};
XZYMap.prototype.zoomOut = function(){
	this.map.zoomOut();
};
XZYMap.prototype.reset = function(){
	this.map.reset();
};
XZYMap.prototype.getDistance = function(start,end){
	return this.map.getDistance(new BMap.Point(start.lng, start.lat),new BMap.Point(end.lng, end.lat));
};
XZYMap.prototype.panTo = function(dest){
	this.map.panTo(new BMap.Point(dest.lng, dest.lat));
};
XZYMap.prototype.panBy = function(pixelX,pixelY){
	this.map.panBy(pixelX,pixelY);
};
XZYMap.prototype.setContextMenu = function(menuArr){
	
};
XZYMap.prototype.openInfoWindow = function(view,opts){
	
};
XZYMap.prototype.closeInfoWindow = function(){
	
};
XZYMap.prototype.getInfoWindow = function(){
	
};
XZYMap.prototype.pointToPixel = function(point){
	return this.map.pointToPixel(new BMap.Point(point.lnt,point.lat));
};
XZYMap.prototype.pixelToPoint = function(pixel){
	return this.map.pixelToPoint(new BMap.Pixel(pixel.x,pixel.y));
};

XZYMap.prototype.addOverlay = function(name,type,opts){
	var overlay = this.overlayMapping[name];
	if(overlay){
		this.map.removeOverlay(overlay);
	}
	overlay = null;
	if(type == XZYMap.OVERLAY_TYPE.MARK_TYPE){
		overlay = {
				markers:[],
				addMarker:this._customOverlay_addMarker,
				addMarkers:this._customOverlay_addMarkers,
				clear: this._customOverlay_clear,
				removeMarker:this._customOverlay_removeMarker
		};
	}else if(type == XZYMap.OVERLAY_TYPE.SHAPE_TYPE){
		
	}else if(type == XZYMap.OVERLAY_TYPE.POINT_COLLECTION){
		overlay = new BMap.PointCollection([],{});
		overlay.addMarkers = this._pointCollection_addMarkers;
	}
	
	if(overlay != null){
		this.overlayMapping[name] = overlay;
		if(opts && opts.click){
			overlay.addEventListener("click",opts.click);
		}
		this.map.addOverlay(overlay);
	}
};
XZYMap.prototype._customOverlay_addMarker = function(marker,opts,instance){
	this.markers[this.markers.length] = marker;
	var w = 32,h=32;
	if(marker.width){
		w = marker.width;
	}
	if(marker.height){
		h = marker.height;
	}
	var icon = new BMap.Icon(marker.url, new BMap.Size(w,h),{anchor:new BMap.Size(w/2,h/2)});
	var m = new BMap.Marker(new BMap.Point(marker.lng,marker.lat),{icon:icon});
	marker.m = m;
	m.m = marker;
	if(marker.click){
		m.addEventListener("click",marker.click);
	}
	var label = null;
	m.setLabel(label = new BMap.Label("<div style='text-align:center;width:96px;font-size:16px'><span style='color:red;box-shadow: 2px 2px 2px 2px rgba(50, 120, 64, 1);'>"+marker.name+"</span></div>",{offset:new BMap.Size((w-96)/2,h)}));
	label.setStyle({backgroundColor:"transparent",border:"0"});
	instance.map.addOverlay(m);
};
XZYMap.prototype._customOverlay_addMarkers = function(markers,opts,instance){
	for(var i=0;i<markers.length;i++){
		this.addMarker(markers[i],opts,instance);
	}
};
XZYMap.prototype._customOverlay_clear = function(instance){
	for(var i=0;i<this.markers.length;i++){
		instance.map.removeOverlay(this.markers[i].m);
	}
	this.markers.length = 0;
};
XZYMap.prototype._customOverlay_removeMarker = function(marker,instance){
	for(var i=0;i<this.markers.length;i++){
		if(marker == this.markers[i]){
			instance.map.removeOverlay(marker.m);
			this.markers.splice(i,1);
		}
	}
};

XZYMap.prototype._pointCollection_addMarkers = function(markers,opts,instance){
	var pArr = [];
	for(var i=0;i<markers.length;i++){
		pArr[i] = new BMap.Point(markers[i].lng, markers[i].lat);
		pArr[i].id = markers[i].id;
	}
	var options = {
	        size: BMAP_POINT_SIZE_SMALL,
	        shape: (opts && opts.shape)?instance._getPointShape(opts.shape):BMAP_POINT_SHAPE_STAR,
	        color: (opts && opts.color)?opts.color:"#d340c3"
	    }
	this.setStyles(options);
	this.setPoints(pArr);
};

XZYMap.prototype.removeOverlay = function(name){
	
};
XZYMap.prototype.clearOverlays = function(){
	
};
XZYMap.prototype.getOverlays = function(){
	
};
XZYMap.prototype.addMarker = function(overlayName,marker,opts){
	var overlay = this.overlayMapping[overlayName];
	if(overlay == null){
		return ;
	}
	overlay.addMarker(marker, opts, this);
};
XZYMap.prototype.addMarkers = function(overlayName,markers,opts){
	var overlay = this.overlayMapping[overlayName];
	if(overlay == null){
		return ;
	}
	overlay.addMarkers(markers, opts, this);
};
XZYMap.prototype._getPointShape = function(shapeType){
	switch(shapeType){
	case XZYMap.POINT_TYPE.STAR:
		return BMAP_POINT_SHAPE_STAR;
	case XZYMap.POINT_TYPE.CYCLE:
		return BMAP_POINT_SHAPE_CIRCLE;
	case XZYMap.POINT_TYPE.SQUARE:
		return BMAP_POINT_SHAPE_SQUARE;
	case XZYMap.POINT_TYPE.RHOMBUS:
		return BMAP_POINT_SHAPE_RHOMBUS;
	case XZYMap.POINT_TYPE.WATERDROP:
		return BMAP_POINT_SHAPE_WATERDROP;
	}
	return BMAP_POINT_SHAPE_STAR;
};
XZYMap.prototype.removeMarker = function(overlayName,marker){
	var overlay = this.overlayMapping[overlayName];
	if(!overlay){
		return ;
	}
	overlay.removeMarker(marker,this);
};
XZYMap.prototype.clearMarkers = function(overlayName){
	var overlay = this.overlayMapping[overlayName];
	if(!overlay){
		return ;
	}
	overlay.clear(this);
};

XZYMap.prototype.addPolyline = function(overlayName,polyline,opts){
	
};
XZYMap.prototype.removePolyline  = function(overlayName,polyline){
	
};
XZYMap.prototype.clearPolylines = function(overlayName){
	
};


XZYMap.prototype.addPolygon = function(overlayName,polygon,opts){
	
};
XZYMap.prototype.removePolygon  = function(overlayName,polygon){
	
};
XZYMap.prototype.clearPolygons = function(overlayName){
	
};


XZYMap.prototype.addEllipse = function(overlayName,ellipse,opts){
	
};
XZYMap.prototype.removeEllipse  = function(overlayName,ellipse){
	
};
XZYMap.prototype.clearEllipses = function(overlayName){
	
};

XZYMap.prototype.addRectangle = function(overlayName,rect,opts){
	
};
XZYMap.prototype.removeRectangle  = function(overlayName,rect){
	
};
XZYMap.prototype.clearRectangles = function(overlayName){
	
};




