package autostepper;

import gnu.trove.list.array.TFloatArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

public class StepGeneratorTest {
    @Test
    public void generateNotesReturnsData() {
        TFloatArrayList[] many = {new TFloatArrayList(), new TFloatArrayList(), new TFloatArrayList(), new TFloatArrayList()};
        TFloatArrayList[] few = {new TFloatArrayList(), new TFloatArrayList(), new TFloatArrayList(), new TFloatArrayList()};
        few[AutoStepper.KICKS].add(0.5f);
        String notes = StepGenerator.GenerateNotes(1, 2, many, few, new TFloatArrayList(), new TFloatArrayList(), 0.01f, 0.5f, 0f, 10f, false);
        assertNotNull(notes);
        assertTrue(notes.contains(","));
    }
}
