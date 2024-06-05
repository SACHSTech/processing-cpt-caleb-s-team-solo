import processing.core.PApplet;

public class Test extends PApplet{
    public void draw() {
        int[] numbers = {1,2,3};
        modifyArray(numbers);
        System.out.println(numbers[0]);
    }

    public void modifyArray(int[] arr){
        arr[0] = 10;
    }
}
