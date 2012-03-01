using AmebaTest.Model;
using Microsoft.Phone.Controls;


namespace AmebaTest
{
    public partial class MainPage : PhoneApplicationPage
    {
        private Calculator calculator;

        // Constructor
        public MainPage()
        {
            InitializeComponent();

            System.Console.WriteLine("add");
            System.Diagnostics.Debug.WriteLine("aaa");
            calculator = new Calculator();
            calculator.add(10, 20);
        }
    }
}