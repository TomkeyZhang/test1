
public class B extends A {
    @Override
    public void show() {
        System.out.println("B show()");
    }
    public static void main(String[] args) {
        A b=new B();
        b.show();
    }
}
