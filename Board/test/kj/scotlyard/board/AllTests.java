package kj.scotlyard.board;

import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ kj.scotlyard.board.util.AllTests.class, UnixPasswordFieldTest.class })
public class AllTests {

}