using AmebaTest.Model;
using Microsoft.VisualStudio.TestTools.UnitTesting;


namespace AmebaTest.Tests.Model
{
    [TestClass]
    public class CalculatorTest
    {

        Calculator calculator;

        [TestInitialize]
        public void SetUp()
        {
            calculator = new Calculator();
        }

        [TestMethod]
        public void addTest()
        {
            Assert.IsTrue(calculator.add(3, 7) == 10);
        }

        [TestMethod]
        public void secondMethodTest()
        {
            Assert.IsTrue(false);
        }

           [TestMethod]
        public void thirdMethodTest()
        {
             Assert.IsTrue(true);
        }



    }
}
