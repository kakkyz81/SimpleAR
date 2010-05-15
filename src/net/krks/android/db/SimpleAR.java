package net.krks.android.db;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

/**
 * ActiveRecord like class
 * @author kakkyz
 *
 */
public abstract class SimpleAR {
	/** id */
	private static final String ID_NAME = "_id";
	/** should override at subclass */
	static int DB_VERSION = 1;
	/** primary key of table (-1 means this object not saved) */
	private long _id = -1;

	private static OpenHelper openHelper;
	private static SQLiteDatabase db;

	private List<Column> columns = new ArrayList<Column>();
	private boolean newinstance;
	String tablename;
	Context appcontext;

	enum ColumnType {
		NULL, INTEGER, REAL, TEXT, BLOB
	}

	/**
	 * テーブルのカラムを示すクラス
	 * 
	 * @author kakkyz
	 * 
	 */
	private static final class Column {
		private ColumnType type;
		private String name;

		public Column(ColumnType type, String name) {
			this.type = type;
			this.name = name;
		}

		public String getTypeString() {
			// TODO 必要なのか(?)
			if (type == ColumnType.BLOB) {
				return "BLOB";
			}
			if (type == ColumnType.INTEGER) {
				return "INTEGER";
			}
			if (type == ColumnType.REAL) {
				return "REAL";
			}
			if (type == ColumnType.TEXT) {
				return "TEXT";
			}
			if (type == ColumnType.NULL) {
				return "NULL";
			}
			return null;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}

	}
	
	/**
	 * コンストラクタ
	 * @param context
	 */
	public SimpleAR(Context context) {
		newinstance = true;
		setUpColumns();
		if (openHelper == null) {
			openHelper = new OpenHelper(context, getClass().getName());
		}
		if (db == null) {
			db = openHelper.getWritableDatabase();
		}
		appcontext = context; // findで使う
	}

	private void setUpColumns() {
		// tablenameの設定がされている場合は、すでに読み込みが行われているとみなす
		if (null != tablename) {
			return;
		}

		tablename = getClass().getName().replaceAll("^.*\\.", "");
		Field[] f = getClass().getDeclaredFields();
		columns.add(new Column(ColumnType.INTEGER, ID_NAME));
		for (Field z : f) {
			columns.add(new Column(getColumnType(z.getType()), z.getName()));
			// Log.d("ActiveRecord", z.toString());
		}
	}

	/**
	 * クラスの型をSQLiteの型にマッピングする
	 * 
	 * サポートするクラス String Integer Long Date Boolean BigDecimal Double Float
	 * 
	 * @param type
	 * @return
	 */
	private ColumnType getColumnType(Class<?> type) {
		if (type == String.class || type == BigDecimal.class) {
			return ColumnType.TEXT;
		}
		if (type == Integer.class || type == Integer.TYPE || type == Long.class
				|| type == Long.TYPE || type == Date.class
				|| type == Boolean.class || type == Boolean.TYPE) {
			return ColumnType.INTEGER;
		}
		if (type == Double.class || type == Double.TYPE || type == Float.class
				|| type == Float.TYPE) {
			return ColumnType.REAL;
		}
		// サポートしていない型の場合
		throw new IllegalArgumentException();
	}

	/**
	 * @return select count(*) from table
	 */
	public long count() {
		StringBuilder sql = new StringBuilder();
		sql.append("select count(*) from ").append(tablename).append(";");
		SQLiteStatement stmt = db.compileStatement(sql.toString());

		return stmt.simpleQueryForLong();
	}

	/**
	 * Where句を自由に指定して検索する
	 * 
	 * @see SQLiteDatabase#query(boolean, String, String[], String, String[],
	 *      String, String, String, String)
	 **/
	public List<SimpleAR> findByQuery(boolean distinct, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		List<SimpleAR> returnList = new ArrayList<SimpleAR>();

		String[] selectcolumns = getColumnNames();
		Cursor c = db.query(distinct, tablename, selectcolumns, selection,
				selectionArgs, groupBy, having, orderBy, limit);

		// マッピング
		SimpleAR record;
		if (c.moveToFirst()) {
			do {
				record = mapping(c);
				returnList.add(record);
			} while (c.moveToNext());
		}

		return returnList;
	}
	public List<? extends SimpleAR> findAll(int limit) {
		return findBy(new HashMap<String, Object>(), limit);
	}

	public List<? extends SimpleAR> findAll() {
		return findBy(new HashMap<String, Object>(), 0);
	}
	
	public List<? extends SimpleAR> findBy(Map<String, Object> conditions) {
		return findBy(conditions, 0);
	}
	/**
	 * 項目を指定してデータを検索する。 サポートするのは where xxx = yyy
	 * の形式のみ。大小演算子(>,<,>=,<=,in,betweenなどはサポート対象外)
	 * yyy部がnullでないこと。nullの場合、nullpointerexceptionがthrowされる。
	 * 
	 * @param conditions
	 * @return
	 */
	public List<? extends SimpleAR> findBy(Map<String, Object> conditions, int limit) {
		if(limit < 0){ throw new IllegalArgumentException("limit must > 0 "); }
		
		List<SimpleAR> returnList = new ArrayList<SimpleAR>();

		StringBuilder where = new StringBuilder();
		ArrayList<String> values = new ArrayList<String>();

		boolean first = true;
		for (Map.Entry<String, Object> e : conditions.entrySet()) {
			if (!first) {
				where.append(" and ");
			}
			first = false;
			where.append(e.getKey());
			where.append(" = ? ");

			Object o = e.getValue();
			if (o == null) {
				throw new NullPointerException();
			} else if (o instanceof Integer || o instanceof Double
					|| o instanceof Float || o instanceof BigDecimal
					|| o instanceof String || o instanceof Long) {
				values.add(o.toString());
			} else if (o instanceof Date) {
				values.add(String.valueOf(((Date) o).getTime()));
			} else if (o instanceof Boolean) {
				if ((Boolean) o) {
					values.add(String.valueOf(BOOLEAN_TRUE));
				} else {
					values.add(String.valueOf(BOOLEAN_FALSE));
				}
			}
		}

		String[] selectcolumns = getColumnNames();
		String[] bindvalues = new String[values.size()];
		values.toArray(bindvalues);

		String limitStr;
		if(limit == 0){
			limitStr = null;
		}else{
			limitStr = String.valueOf(limit);
		}
		Cursor c = db.query(tablename, selectcolumns, where.toString(),
				bindvalues, null, // groupby
				null, // having
				null, // ordeby
				limitStr
				);

		// マッピング
		SimpleAR record;
		if (c.moveToFirst()) {
			do {
				record = mapping(c);
				returnList.add(record);
			} while (c.moveToNext());
		}

		return returnList;
	}

	/**
	 * exec "delete from table"
	 */
	public void truncate() {
		StringBuilder sql = new StringBuilder();
		sql.append("delete from ").append(tablename).append(";");
		db.execSQL(sql.toString());
	}

	private SimpleAR mapping(Cursor c) {
		SimpleAR record;
		// try {
		try {
			Constructor<? extends SimpleAR> constructor = this.getClass()
					.getConstructor(Context.class);
			record = (SimpleAR) constructor.newInstance(appcontext);
			record.newinstance = false;
			int max = c.getColumnCount();
			for (int i = 0; i < max; i++) {
				// _idだけは親クラスで宣言しているので、DeclaredFieldでは読み取りできないため、ロジックを入れている。
				if (c.getColumnName(i).equals(ID_NAME)) {
					record._id = c.getLong(i);
					continue;
				}
				// 子クラスで宣言したフィールドのマッピング
				Field f = record.getClass()
						.getDeclaredField(c.getColumnName(i));
				f.setAccessible(true);
				if (c.isNull(i)) {
					f.set(record, null);
				} else if (String.class == f.getType()) {
					// String
					f.set(record, c.getString(i));
				} else if (Integer.class == f.getType()
						|| Integer.TYPE == f.getType()) {
					// Integer
					f.set(record, c.getInt(i));
				} else if (Long.class == f.getType()
						|| Long.TYPE == f.getType()) {
					// Long
					f.set(record, c.getLong(i));
				} else if (Double.class == f.getType()
						|| Double.TYPE == f.getType()) {
					// Double
					f.set(record, c.getDouble(i));
				} else if (Float.class == f.getType()
						|| Float.TYPE == f.getType()) {
					// Float
					f.set(record, c.getFloat(i));
				} else if (Boolean.class == f.getType()
						|| Boolean.TYPE == f.getType()) {
					// Boolean
					if (c.getInt(i) == BOOLEAN_TRUE) { // TODO
						f.set(record, true);
					} else {
						f.set(record, false);
					}
				} else if (Date.class == f.getType()) {
					// Date
					f.set(record, new Date(c.getLong(i)));
				} else if (BigDecimal.class == f.getType()) {
					// BigDecimal
					f.set(record, new BigDecimal(c.getString(i)));
				}
			}
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		// } catch (Exception e) {
		// throw new RuntimeException(e);
		// }
		return record;
	}

	/**
	 * テーブルのコラム名一覧をString配列で得る
	 * 
	 * @return
	 */
	private String[] getColumnNames() {
		if (columns == null) {
			throw new IllegalStateException("columns is null");
		}

		ArrayList<String> columnname = new ArrayList<String>();
		for (Column c : columns) {
			columnname.add(c.getName());
		}

		return (String[]) columnname.toArray(new String[0]);

	}

	/**
	 * keyをセットして値を得る
	 * 
	 * @param id _id
	 * @return SimpleAR object
	 * @throws FileNotFoundException
	 */
	public SimpleAR find(long id) throws FileNotFoundException {

		String[] selectcolumns = getColumnNames();
		Cursor c = db.query(tablename, selectcolumns, "_id = ?",
				new String[] { String.valueOf(id) }, null, // groupby
				null, // having
				null // ordeby
				);

		// マッピング
		SimpleAR record;
		if (c.moveToFirst()) {
			record = mapping(c);
			record._id = id;
		} else {
			throw new FileNotFoundException();
		}

		return record;
	}
	/**
	 * delete
	 * @return
	 */
	public void delete(){
		StringBuilder sql = new StringBuilder();
		sql.append("delete from ").append(tablename).append(" where _id = ").append(_id).append(";");
		db.execSQL(sql.toString());
	}
	
	/**
	 * insert,update時はidが返る。失敗したときは、-1が返る
	 * 
	 * @return id or -1
	 */
	public long save() {
		ContentValues values = new ContentValues();

		try {
			for (Column c : columns) {
				if (c.name.equals(ID_NAME)) {
					continue;
				}
				Field f = getClass().getDeclaredField(c.name);
				f.setAccessible(true);
				Object o = f.get(this);
				if (o == null) {
					values.putNull(c.getName());
				} else if (c.type == ColumnType.TEXT) {
					if (o instanceof String) {
						values.put(c.getName(), (String) o);
					} else if (o instanceof BigDecimal) {
						values.put(c.getName(), ((BigDecimal) o)
								.toPlainString());
					}
				} else if (c.type == ColumnType.INTEGER) {
					if (o instanceof Integer) {
						values.put(c.getName(), (Integer) o);
					} else if (o instanceof Long) {
						values.put(c.getName(), (Long) o);
					} else if (o instanceof Date) {
						values.put(c.getName(), ((Date) o).getTime());
					} else if (o instanceof Boolean) {
						if ((Boolean) o) {
							values.put(c.getName(), BOOLEAN_TRUE);
						} else {
							values.put(c.getName(), BOOLEAN_FALSE);
						}
					}
				} else if (c.type == ColumnType.REAL) {
					if (o instanceof Double) {
						values.put(c.getName(), (Double) o);
					} else if (o instanceof Float) {
						values.put(c.getName(), (Float) o);
					}
				}
			}
			if (newinstance) {
				_id = db.insert(tablename, null, values);
			} else {
				if (0 != db.update(tablename, values, "_id = ?",
						new String[] { String.valueOf(_id) })) {
					return -1;
				}
			}
			return _id;
		} catch (Exception e) { // TODO 例外処理の取り扱い再検討
			throw new RuntimeException(e);
		}
	}

	private static final int BOOLEAN_TRUE = 1;
	private static final int BOOLEAN_FALSE = 0;

	/**
	 * テーブルをドロップする ドロップしたテーブルは、再度 openHelper.onCreate(db) でクリエイトしなおす必要がある。
	 */
	public void drop() {
		StringBuilder sql = new StringBuilder();
		sql.append("drop table ").append(tablename).append(";");
		db.execSQL(sql.toString());
	}

	/**
	 * OpenHelper. サブクラスのフィールドから、テーブルを作成する
	 * 
	 * @author kakkyz
	 * 
	 */
	private class OpenHelper extends SQLiteOpenHelper {

		public OpenHelper(Context context, String name) {
			super(context, name, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			StringBuilder sql = new StringBuilder();
			sql.append("create table ").append(tablename).append(
					"( _id integer primary key autoincrement");
			for (Column c : columns) {
				if (c.name.equals(ID_NAME)) {
					continue;
				}
				sql.append(",").append(c.getName()).append(" ").append(
						c.getTypeString());
			}
			sql.append(");");
			db.execSQL(sql.toString());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	public static SQLiteDatabase getDb() {
		return db;
	}

	public static OpenHelper getOpenHelper() {
		return openHelper;
	}

	public String getTablename() {
		return tablename;
	}

	public long getId() {
		return _id;
	}

	public boolean isNewinstance() {
		return newinstance;
	}

}
