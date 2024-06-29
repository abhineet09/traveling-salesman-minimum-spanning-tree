package andrew.cmu.edu.abhineec;

/******************************************************************************
 * Crime class represents a POJO for each individual crime.
 ******************************************************************************/
public class Crime {
    public double x, y, latitude, longitude;
    public int tract;
    public String time, date, street, offense;

    /*
        X, Y, Time, Street, Offense, Date, Tract, Lat, Long
     */
    /**
     * Initialize a Crime object
     * @param dataEntry
     *      line from a csv file representing a crime
     * @precondition
     * dataEntry must follow (X, Y, Time, Street, Offense, Date, Tract, Lat, Long) format
     * @postcondition
     *   An empty SinglyLinkedList object is initialized
     **/
    public Crime(String dataEntry){
        String[] dataArr = dataEntry.split(",");
        this.x =  Double.parseDouble(dataArr[0].trim());
        this.y =  Double.parseDouble(dataArr[1].trim());
        this.time = dataArr[2].trim();
        this.street = dataArr[3].trim();
        this.offense = dataArr[4].trim();
        this.date = dataArr[5].trim();
        this.tract = Integer.parseInt(dataArr[6].trim());
        this.latitude = Double.parseDouble(dataArr[7].trim());
        this.longitude = Double.parseDouble(dataArr[8].trim());
    }

    /**
     * Helper function to format and return string representation of a crime
     */
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(this.x);
        str.append(", ");
        str.append(this.y);
        str.append(", ");
        str.append(this.time);
        str.append(", ");
        str.append(this.street);
        str.append(", ");
        str.append(this.offense);
        str.append(", ");
        str.append(this.date);
        str.append(", ");
        str.append(this.tract);
        str.append(", ");
        str.append(this.latitude);
        str.append(", ");
        str.append(this.longitude);
        return str.toString();
    }
}
