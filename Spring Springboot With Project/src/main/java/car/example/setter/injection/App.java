package car.example.setter.injection;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {
    public static void main() {
        ApplicationContext context
                = new ClassPathXmlApplicationContext("applicationSetterInjection.xml");
        Car myCar2 = (Car)context.getBean("myCar2");
        myCar2.displayDetails();
    }
}
