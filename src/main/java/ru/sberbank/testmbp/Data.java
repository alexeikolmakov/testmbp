package ru.sberbank.testmbp;

public class Data {

    private final String code;

    private final String data;


    public Data(String code, String data) {
        this.code = code;
        this.data = data;
    }


    public String getCode() {
        return code;
    }

    public String getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Data data = (Data) o;

        return code.equals(data.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
