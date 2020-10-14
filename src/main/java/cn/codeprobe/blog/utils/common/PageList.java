package cn.codeprobe.blog.utils.common;

import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

public class PageList<T> implements Serializable {

    private int currentPage;

    private long totalCount;

    private long pageSize;

    private long totalPage;

    private boolean isFirst;

    private boolean isLast;

    private List<T> contents;

    public PageList(int page, int size, long totalCount) {
        this.totalCount = totalCount;
        this.currentPage = page;
        this.pageSize = size;
        this.totalPage = this.totalCount % this.pageSize == 0 ? this.totalCount / this.pageSize : (this.totalCount / this.pageSize) + 1;
        this.isFirst = this.currentPage == 1;
        this.isLast = this.currentPage == totalPage;
    }

    public PageList() {

    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public List<T> getContents() {
        return contents;
    }

    public void setContents(List<T> contents) {
        this.contents = contents;
    }

    public void parsePage(Page<T> all) {
        setTotalPage(all.getTotalPages());
        setContents(all.getContent());
        setCurrentPage(all.getNumber() + 1);
        setPageSize(all.getTotalElements());
        setFirst(all.isFirst());
        setLast(all.isLast());
    }
}
