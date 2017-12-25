package com.bixin.bixin.database.model;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

/**
 * Title: ModelObject.java
 * Description: 数据库模型基类
 * @version 1.0
 */
public class ModelObject implements Serializable{

	public static final String FIELD_ID = "_id";

	private static final long serialVersionUID = 7400085919033475355L;
    
    @DatabaseField(columnName = FIELD_ID, generatedId = true)
    protected long id = -1;
    
    protected ModelObject() {
    }

	public long getId() {
		return id;
	}

	public ModelObject setId(long id) {
		this.id = id;
		return this;
	}
}
