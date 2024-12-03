package processor.memorysystem;


public class CacheLine{
    //to store tag address
    int[] tag = new int[2]; 
    //to store data at that address
    int[] data = new int[2];
    int least_recently_used;

    public CacheLine() {
        this.tag[0] = -1;
        this.tag[1] = -1;
        this.least_recently_used = 0;
    }

    public void setleastrecentlyused(int a){
        this.least_recently_used = a;
    }

    public int getleastrecentlyused(int a){
        return this.least_recently_used;
    }

    public void setValue(int tag, int value) {
        if(tag == this.tag[1]) {
            this.data[1] = value;
            this.least_recently_used = 0;
        }
        else if(tag == this.tag[0]) {
            this.data[0] = value;
            this.least_recently_used = 1;
        }
        else {
            this.tag[this.least_recently_used] = tag;
            this.data[this.least_recently_used] = value;
            this.least_recently_used = 1 - this.least_recently_used;
        }
	}

    // public int getValue(int tag){
    //     if(tag == this.tag[1]) {
    //         return this.data[1];
    //     }
    //     else{
    //         return this.data[0];
    //     }
    // }

    public int getData(int i){
        return this.data[i];
    }

}