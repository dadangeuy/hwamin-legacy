package com.rizaldi.hwamin;

import com.rizaldi.hwamin.game.duaempat.DuaEmpatLogicService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DuaEmpatTest {
    @Autowired DuaEmpatLogicService logic;

    @Test
    public void testSolve() {
        String sol = logic.getSolution(Arrays.asList(1, 1, 1, 12));
        Assert.assertNotEquals(sol, "tidak ada");
        sol = logic.getSolution(Arrays.asList(31, 17, 99, 13));
        Assert.assertEquals(sol, "tidak ada");
        sol = logic.getSolution(Arrays.asList(12, 1, 24, 12));
        Assert.assertNotEquals(sol, "tidak ada");
    }

    @Test
    public void testAnswer() throws Exception {
        boolean sol = logic.isCorrectAnswer(Arrays.asList(1, 1, 1, 12), "(1+1)*1*12");
        Assert.assertEquals(sol, true);
        sol = logic.isCorrectAnswer(Arrays.asList(1, 1, 1, 12), "(1-1)*1*12");
        Assert.assertEquals(sol, false);
    }
}
