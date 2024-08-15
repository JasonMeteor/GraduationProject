package mobileapp.graduationproject;

public class MinusRequest {
    private int number1;
    private int number2;

    public MinusRequest(int num1, int num2) {
        this.number1 = num1;
        this.number2 = num2;
    }

    public int getNum1() {
        return number1;
    }

    public void setNum1(int num1) {
        this.number1 = num1;
    }

    public int getNum2() {
        return number2;
    }

    public void setNum2(int num2) {
        this.number2 = num2;
    }
}
