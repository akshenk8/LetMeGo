function getPolylineIntersection() {
    var commonPts = [];
    for (var i = 0; i < polyline1.getPath().getLength(); i++) {
        for (var j = 0; j < polyline2.getPath().getLength(); j++) {
            if (polyline1.getPath().getAt(i).equals(polyline2.getPath().getAt(j))) {
                commonPts.push({
                    lat: polyline1.getPath().getAt(i).lat(),
                    lng: polyline1.getPath().getAt(i).lng(),
                    route1idx: i
                });
            }
        }
    }
    var path = [];
    var prevIdx = commonPts[0].route1idx;
    for (var i = 0; i < commonPts.length; i++) {
        if (commonPts[i].route1idx <= prevIdx + 1) {
            path.push(commonPts[i]);
            prevIdx = commonPts[i].route1idx;
        } else {
            var polyline = new google.maps.Polyline({
                map: map,
                path: path,
                strokeWeight: 8,
                strokeColor: "#ff0000"
            });
            path = [];
            prevIdx = commonPts[i].route1idx;
        }
    }
    var polyline = new google.maps.Polyline({
        map: map,
        path: path,
        strokeWeight: 8,
        strokeColor: "#ff0000"
    });

}