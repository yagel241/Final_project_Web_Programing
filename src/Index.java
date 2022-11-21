
import java.io.Serializable;
import java.util.Objects;

/**
 *     Index class implements Serializable and Comparable<Index>
 *     the implementation of Serializable is for Send data as bytes
 *     and Read data as bytes then transform to meaningful data.
 *     the implementation of Comparable<Index> is for
 *     Index to have the ability to be Comparable
 */
public class Index implements Serializable, Comparable<Index> {
    Integer row, column, value;

    // Constructor
    public Index(int oRow, int oColumn) {
        this.row = oRow;
        this.column = oColumn;
        this.value = 0;
    }

    // Constructor
    public Index(int oRow, int oColumn, int value){
        this.row = oRow;
        this.column = oColumn;
        this.value = value;
    }

    // getter
    public int getRow() {
        return row;
    }
    // getter
    public int getColumn() {
        return column;
    }
    // getter
    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "(" + row + "," + column + ")";
    }

    /**
     *
     * @param o - some object probably index
     * @return - if Indexes are equal
     * checking if the indexes have the same row and column
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Index index = (Index) o;
        return Objects.equals(row, index.row) &&
                Objects.equals(column, index.column);
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    /**
     *
     * @param other - Index
     * @return - the difference between this value to other value
     */
    @Override
    public int compareTo(Index other) {
        if (this.value == null || other.getValue() == null) {
            return -1;
        }
        return this.value - other.value;
    }

}