public class Triplet<L,M,R> {

  private final L left;
  private final M middle;
  private final R right;

  public Triplet(L left,M middle, R right) {
    this.left = left;
    this.middle = middle;
    this.right = right;
  }

  public L getLeft() {return this.left;}
  public M getMiddle() {return this.middle;}
  public R getRight() {return this.right;}
}