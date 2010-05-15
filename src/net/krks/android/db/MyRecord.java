package net.krks.android.db;

import java.math.BigDecimal;
import java.util.Date;

import android.content.Context;

public class MyRecord extends SimpleAR {

	public MyRecord(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	String str1;
	String str2;
	String strNull;

	int int1;
	Integer int2;
	Integer intNull;

	long long1;
	Long long2;
	Long longNull;

	double double1;
	Double double2;
	Double doubleNull;
	
	float float1;
	Float float2;
	Float floatNull;
	
	BigDecimal bigdecimal;
	BigDecimal bigdecimalNull;

	boolean boolean1;
	Boolean boolean2;
	boolean boolean3;
	Boolean boolean4;
	Boolean booleanNull;

	Date date1;
	Date date2;
	
	
	
	public Integer getIntNull() {
		return intNull;
	}
	public void setIntNull(Integer intNull) {
		this.intNull = intNull;
	}
	public Long getLongNull() {
		return longNull;
	}
	public void setLongNull(Long longNull) {
		this.longNull = longNull;
	}
	public Double getDoubleNull() {
		return doubleNull;
	}
	public void setDoubleNull(Double doubleNull) {
		this.doubleNull = doubleNull;
	}
	public Float getFloatNull() {
		return floatNull;
	}
	public void setFloatNull(Float floatNull) {
		this.floatNull = floatNull;
	}
	public BigDecimal getBigdecimalNull() {
		return bigdecimalNull;
	}
	public void setBigdecimalNull(BigDecimal bigdecimalNull) {
		this.bigdecimalNull = bigdecimalNull;
	}
	public Boolean getBooleanNull() {
		return booleanNull;
	}
	public void setBooleanNull(Boolean booleanNull) {
		this.booleanNull = booleanNull;
	}
	public boolean getBoolean3() {
		return boolean3;
	}
	public void setBoolean3(boolean boolean3) {
		this.boolean3 = boolean3;
	}
	public Boolean getBoolean4() {
		return boolean4;
	}
	public void setBoolean4(Boolean boolean4) {
		this.boolean4 = boolean4;
	}

	// for test getter & setter method! 
	
	public String getStr1() {
		return str1;
	}
	public void setStr1(String str1) {
		this.str1 = str1;
	}
	public String getStr2() {
		return str2;
	}
	public void setStr2(String str2) {
		this.str2 = str2;
	}
	public int getInt1() {
		return int1;
	}
	public void setInt1(int int1) {
		this.int1 = int1;
	}
	public Integer getInt2() {
		return int2;
	}
	public void setInt2(Integer int2) {
		this.int2 = int2;
	}
	public long getLong1() {
		return long1;
	}
	public void setLong1(long long1) {
		this.long1 = long1;
	}
	public Long getLong2() {
		return long2;
	}
	public void setLong2(Long long2) {
		this.long2 = long2;
	}
	public double getDouble1() {
		return double1;
	}
	public void setDouble1(double double1) {
		this.double1 = double1;
	}
	public Double getDouble2() {
		return double2;
	}
	public void setDouble2(Double double2) {
		this.double2 = double2;
	}
	public float getFloat1() {
		return float1;
	}
	public void setFloat1(float float1) {
		this.float1 = float1;
	}
	public Float getFloat2() {
		return float2;
	}
	public void setFloat2(Float float2) {
		this.float2 = float2;
	}
	public BigDecimal getBigdecimal() {
		return bigdecimal;
	}
	public void setBigdecimal(BigDecimal bigdecimal) {
		this.bigdecimal = bigdecimal;
	}
	public boolean getBoolean1() {
		return boolean1;
	}
	public void setBoolean1(boolean boolean1) {
		this.boolean1 = boolean1;
	}
	public Boolean getBoolean2() {
		return boolean2;
	}
	public void setBoolean2(Boolean boolean2) {
		this.boolean2 = boolean2;
	}
	public Date getDate1() {
		return date1;
	}
	public void setDate1(Date date1) {
		this.date1 = date1;
	}
	public Date getDate2() {
		return date2;
	}
	public void setDate2(Date date2) {
		this.date2 = date2;
	}
	public String getStrNull() {
		return strNull;
	}
	public void setStrNull(String strNull) {
		this.strNull = strNull;
	}
}
