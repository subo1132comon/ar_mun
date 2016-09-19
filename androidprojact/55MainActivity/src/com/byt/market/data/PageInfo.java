
package com.byt.market.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 分页信息基类
 */
@DatabaseTable(tableName="dPageInfo")
public class PageInfo {

    /**
     * 当前页索引
     */
	@DatabaseField
    private int pageIndex; // 当前页索引，从 0 开始 -1表示最后一页
	@DatabaseField
    private int pageNum; // 总页数
	@DatabaseField
    private int pageSize = 20; // 每页显示数量
	@DatabaseField
    private int recordNum; // 总记录数

    /**
     * 取得下一页
     */
    public int getNextPageIndex() {
        return this.pageIndex + 1;
    }

    /**
     * 设置当前页号
     * 
     * @param pageindex
     */
    public int getPageIndex() {
        return this.pageIndex;
    }

    public void setPageIndex(int pageindex) {
        this.pageIndex = pageindex;
    }

    /**
     * 总页数
     * 
     * @return
     */
    public int getPageNum() {
        return this.pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    /**
     * 每页显示数量
     * 
     * @return
     */
    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 总记录数
     * 
     * @return
     */
    public int getRecordNum() {
        return this.recordNum;
    }

    public void setRecordNum(int recordNum) {
        this.recordNum = recordNum;
    }

    @Override
    public String toString() {
        return "PageInfo [pageIndex=" + pageIndex + ", pageNum="
                + pageNum + ", pageSize="
                + pageSize + ", recordNum=" + recordNum + "]";
    }
}
