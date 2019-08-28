package com.ftrend.zgp.model;

import com.dbflow5.annotation.Column;
import com.dbflow5.annotation.PrimaryKey;
import com.dbflow5.annotation.Table;
import com.ftrend.zgp.utils.db.DBHelper;

/**
 * 专柜商品类别
 *
 * @author liziqiang@ftrend.cn
 */
@Table(database = DBHelper.class)
public class DepCls {
    @PrimaryKey(autoincrement = true)
    private int id;
    @Column
    private String depCode;
    @Column
    private String clsCode;
    @Column
    private String clsName;

    public DepCls() {
    }

    public DepCls(String depCode, String clsCode, String clsName) {
        this.depCode = depCode;
        this.clsCode = clsCode;
        this.clsName = clsName;
    }

    public int getID() {
        return id;
    }

    public void setID(int ID) {
        id = ID;
    }

    public String getDepCode() {
        return depCode;
    }

    public void setDepCode(String depCode) {
        this.depCode = depCode;
    }

    public String getClsCode() {
        return clsCode;
    }

    public void setClsCode(String clsCode) {
        this.clsCode = clsCode;
    }

    public String getClsName() {
        return clsName;
    }

    public void setClsName(String clsName) {
        this.clsName = clsName;
    }
}
