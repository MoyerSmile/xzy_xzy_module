package com.xzy.base;

public class Geo_Util {
	public static final double PI = 3.1415926535897932384626;
	public static final double x_PI = PI * 3000.0 / 180.0;
	public static final double EARTH_RADIUS = 6378245.0;
	public static final double EE = 0.00669342162296594323;

	/*
	 * wgs84坐标转换火星(GCJ坐标)坐标
	 */
	public static double[] wgs2Gcj(double wgLat, double wgLon) {
		double mgLat = 0, mgLon = 0;
		if (outOfChina(wgLat, wgLon)) {
			mgLat = wgLat;
			mgLon = wgLon;
			return new double[] { mgLat, mgLon };
		}

		double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
		double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
		double radLat = wgLat / 180.0 * PI;
		double magic = Math.sin(radLat);
		magic = 1 - EE * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0)
				/ ((EARTH_RADIUS * (1 - EE)) / (magic * sqrtMagic) * PI);
		dLon = (dLon * 180.0)
				/ (EARTH_RADIUS / sqrtMagic * Math.cos(radLat) * PI);

		return new double[] { wgLat + dLat, wgLon + dLon };
	}

	/*
	 * 火星坐标（GCJ）转化为百度坐标（BD）
	 */
	public static double[] gcj2BD(double gg_lat, double gg_lon) {
		double x = gg_lon, y = gg_lat;
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_PI);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_PI);

		return new double[] { z * Math.sin(theta) + 0.006,
				z * Math.cos(theta) + 0.0065 };
	}

	/*
	 * wgs84左边转换成BD-09 纬度、经度
	 */
	public static double[] wgs2BD(double wgLat, double wgLon) {
		double[] mg_gps = wgs2Gcj(wgLat, wgLon);
		if (outOfChina(wgLat, wgLon)) {
			return mg_gps;
		}
		return gcj2BD(mg_gps[0], mg_gps[1]);
	}

	/**
	 * 经纬度是否出了中国范围
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	private static boolean outOfChina(double lat, double lon) {
		if (lon < 72.004 || lon > 137.8347)
			return true;
		if (lat < 0.8293 || lat > 55.8271)
			return true;
		return false;
	}

	private static double transformLat(double x, double y) {
		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
				+ 0.2 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
		return ret;
	}

	private static double transformLon(double x, double y) {
		double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
				* Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0
				* PI)) * 2.0 / 3.0;
		return ret;
	}

	/**
	 * 获取两个经纬度之间的直线距离
	 * 
	 * @param lon1
	 *            单位度
	 * @param lat1
	 *            单位度
	 * @param lon2
	 *            单位度
	 * @param lat2
	 *            单位度
	 * @return 距离，单位米
	 */
	public static int getDistance(double lon1, double lat1, double lon2, double lat2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lon1) - rad(lon2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		return (int) Math.round(s);
	}

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}
}
