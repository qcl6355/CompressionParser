import gnu.trove.TObjectIntHashMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Sheng Li
 *         Created on 14/12/8.
 */
public class Alphabet implements Serializable{
    TObjectIntHashMap<String> map;
    boolean growthStopped;
    int numEntries;

    public Alphabet(int capacity){
        this.map = new TObjectIntHashMap<String>(capacity);
    }

    public Alphabet() {
        this(1000);
    }

    public int lookupIndex(String entry, boolean addIfNotPresent){
        if (entry == null) {
            throw new IllegalArgumentException("Can't lookup \"null\" in Alphabet");
        }
        int ret = map.get(entry) == 0 ? -1 : map.get(entry);
        if (ret == -1 && addIfNotPresent && !growthStopped) {
            numEntries++;
            map.put(entry, numEntries);
            ret = numEntries;
        }
        return ret;
    }

    public int lookupIndex(String entry) {
        return lookupIndex(entry, true);
    }

    public void stopGrowth() {
        this.growthStopped = true;
    }

    public void allowGrowth() {
        this.growthStopped = false;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.numEntries);
        out.writeBoolean(this.growthStopped);
        out.writeObject(this.map);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.numEntries = in.readInt();
        this.growthStopped = in.readBoolean();
        this.map = (TObjectIntHashMap<String>) in.readObject();
    }

    public int size() {
        return numEntries;
    }
}
