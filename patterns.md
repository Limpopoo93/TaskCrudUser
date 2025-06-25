SOLID
GoF
GRASP

<a href="https://mc.yandex.ru/pixel/8711235002931986822?rnd=%aw_random%">
    <img src="https://mc.yandex.ru/pixel/8711235002931986822?rnd=%aw_random%" />        
  </a>&nbsp;&nbsp;
<a href="https://mc.yandex.ru/watch/92801430">
    <img src="https://mc.yandex.ru/watch/92801430" />        
  </a>&nbsp;&nbsp;

Если вам интересно мое резюме: https://github.com/DEBAGanov

# Patterns
- [Patterns](#Patterns)
  - [Что такое SOLID и расшифровка?](#Что_такое_SOLID_и_расшифровка)
  - [Какие есть типы паттернов?](#Какие_есть_типы_паттернов)
  - [Приведите примеры порождающих шаблонов?](#Приведите_примеры_порождающих_шаблонов)
  - [Приведите примеры структурных шаблонов?](#Приведите_примеры_структурных_шаблонов)
  - [Приведите примеры поведенческих шаблонов?](#Приведите_примеры_поведенческих_шаблонов)
  - [Что такое антипаттерн и какие знаете?](#Что_такое_антипаттерн_и_какие_знаю)

## Что_такое_SOLID_и_расшифровка

SOLID - это аббревиатура, используемая для описания пяти основных принципов объектно-ориентированного программирования (ООП), 
которые помогают разработчикам создавать более поддерживаемый и расширяемый код.
1. Принцип единственной ответственности (Single Responsibility Principle, SRP) - класс должен иметь только одну 
   ответственность. Например, класс, отвечающий за работу с базой данных, не должен также заниматься обработкой 
   пользовательского ввода или выводом на экран.
     Можно упомянуть, как распознать проблему в коде:
     - Класс имеет больше 100–200 строк кода.
     - Методы класса логически несвязаны (например, sendEmail() и validatePayment()).
     - При изменении одной части кода ломается другая, казалось бы, не связанная с ней.

2. Принцип открытости/закрытости (Open/Closed Principle, OCP) - классы должны быть открыты для расширения, но закрыты для 
   модификации. Это означает, что новый функционал должен добавляться через добавление новых классов или методов, 
   а не изменение существующих.
   Пример нарушения OCP:
   `class DiscountCalculator {
      public double calculateDiscount(String customerType) {
          if (customerType.equals("REGULAR")) {
              return 0.1; // 10% скидка
          } else if (customerType.equals("VIP")) {
              return 0.2; // 20% скидка
          }
          return 0;
      }
    }`
   Проблемы:
   При добавлении нового типа клиента (например, "PREMIUM") нужно лезть внутрь класса и изменять метод calculateDiscount.
   Как исправить?
   Решение 1: Через интерфейсы и полиморфизм
   `interface DiscountStrategy {
       double calculateDiscount();
    }

    class RegularDiscount implements DiscountStrategy {
       public double calculateDiscount() { return 0.1; }
    }
    
    class VipDiscount implements DiscountStrategy {
       public double calculateDiscount() { return 0.2; }
    }
    
    class DiscountCalculator {
       private DiscountStrategy strategy;

       public DiscountCalculator(DiscountStrategy strategy) {
        this.strategy = strategy;
       }
   
       public double calculateDiscount() {
           return strategy.calculateDiscount();
       }
    }`

3. Принцип подстановки Барбары Лисков (Liskov Substitution Principle, LSP) - класс который наследовался от родителя не должен 
   ломать интерфейс. Допустим если мы наследовались от класс который не бросает Exception а я бросаю Exception то 
   родительский класс может этого не знать и у него будет не объяснимое поведение.
   `Проблемный код
     class Rectangle {
         protected int width;
         protected int height;
      
         public void setWidth(int width) {
           this.width = width;
         }
      
         public void setHeight(int height) {
           this.height = height;
         }
      
         public int calculateArea() {
           return width * height;
         }
     }

     class Square extends Rectangle {
        @Override
        public void setWidth(int width) {
           this.width = width;
           this.height = width; // Нарушение! Квадрат меняет и ширину, и высоту
        }

        @Override
        public void setHeight(int height) {
            this.width = height; // Нарушение!
            this.height = height;
        }
     }`
   Если клиентский код работает с Rectangle и ожидает, что setWidth() и setHeight() изменяют только одну сторону, 
   подстановка Square ломает логику:

   `Rectangle rect = new Square();
   rect.setWidth(5);
   rect.setHeight(10);
   System.out.println(rect.calculateArea()); // 100? Нет, 25! (Ожидалось 50)`
   Вывод: Square не является корректным подтипом Rectangle, потому что меняет поведение базового класса.
   Как исправить? Соблюдение LSP
   Решение 1: Отказ от наследования
   Решение 2: Абстрактный класс + неизменяемые объекты

4. Принцип разделения интерфейса (Interface Segregation Principle, ISP) - клиенты не должны зависеть от интерфейсов, которые
   они не используют. Интерфейсы должны быть маленькими и специфическими для конкретных задач.
   Пример нарушения ISP
   Проблемный код:
   `interface Worker {
      void work();
      void eat();
      void sleep();
   }

   class OfficeWorker implements Worker {
     public void work() { /* Работа в офисе */ }
     public void eat() { /* Обед в кафе */ }
     public void sleep() { /* Сон дома (но зачем это работнику?) */ }
   }

   class Robot implements Worker {
      public void work() { /* Автоматизированная работа */ }
      public void eat() { throw new UnsupportedOperationException(); } // Нарушение!
      public void sleep() { throw new UnsupportedOperationException(); } // Нарушение!
   }`
Проблемы:
Robot вынужден реализовывать eat() и sleep(), хотя они ему не нужны.
Это приводит к:
Избыточному коду (пустые методы или исключения).
Нарушению LSP (робот — не совсем Worker, если не может есть и спать).

Как исправить? Применение ISP
Решение: Разделение интерфейсов

5. Принцип инверсии зависимостей (Dependency Inversion Principle, DIP) - класс должен зависеть не от реализации а от 
   абстракций. Тоесть класс должен быть написан так чтобы он зависел не от конкретной реализации а от интерфейса.
   Например, класс, который использует базу данных, должен зависеть от абстрактного интерфейса базы данных, а не от конкретной 
   реализации базы данных. к примеру еще Spring тоже реализует этот принцип
   Проблемный код:
   `class OrderService {
      private MySQLDatabase database;  // Прямая зависимость от конкретного класса!

      public OrderService() {
         this.database = new MySQLDatabase();  // Жесткая привязка
      }
   
      public void saveOrder(Order order) {
          database.save(order);
      }
   }`
   Проблемы:
   - OrderService жестко зависит от MySQLDatabase.
   - Если нужно перейти на PostgreSQL, придется переписывать код.
   - Сложно тестировать: нельзя подменить MySQLDatabase моком.
   Как исправить? Применение DIP
   - Решение 1: Внедрение зависимости через интерфейс

## Какие_есть_типы_паттернов

1. Порождающие - абстрагируют процесс создание экземпляра. Они позволяют сделать систему 
   независимой от способа создания, композиции и представления объектов. 
   Шаблон, порождающий классы, использует наследование, чтобы изменять созданный объект, а шаблон, порождающий объекты, 
   делегирует создание объектов другому объекту.
2. Структурные - определяют различные сложные структуры, которые изменяют интерфейс уже существующих объектов или его 
   реализацию, позволяя облегчить разработку и оптимизировать программу.
3. Поведения - определяют взаимодействие между объектами, увеличивая таким образом его гибкость.
4. Антишаблоны

## Приведите_примеры_порождающих_шаблонов

1. Абстрактная фабрика (Abstract factory) - заключается в предоставлении интерфейса для создания множества взаимосвязанных или зависимых объектов, без строгой привязки к конкретным классам.
2. Строитель (Builder) - отделить конструирование сложного объекта от его представления таким образом чтобы мы могли получить разные представления. Реализовано в Spring через аннотация @Builder()
3. Фабричный метод (Factory method) - Определяет интерфейс для создания объекта, но оставляет подклассам решение о том, какой класс создавать.
4. Прототип (Prototype) - Определяет интерфейс создания объекта через клонирование другого объекта вместо создания через 
   конструктор.
5. Одиночка (Singleton) - Класс, который может иметь только один экземпляр.

## Приведите_примеры_структурных_шаблонов

1. Адаптер (Adapter) - Позволяет несовместимым интерфейсам работать вместе.
   Пример: InputStreamReader (адаптирует InputStream к Reader).
   Ниже пример когда мы имплементим один интерфейс а второй в конструкторе.
   `interface Car(){}
    interface Locomotive(){}
    class ElectroCar implemets Car(){}
    class ElectricLocomotive implements Locomotive() {}
    class CarToLocomotive implemets Locomotive {
       private Car car;
    }
   `
2. Мост (Bridge) - Структура, позволяющая изменять интерфейс обращения и интерфейс реализации класса независимо.
   Вкратце: Есть интерфейс с методом, мы его через конструктор можем передать в абстрактный класс без реализации методов а
   потом уже в классе в котором будем реализовывать абстрактный класс у нас будут доступны методы как абстрактного класса так и
   интерфейса.
   `
   public abstract class Shape {
     public abstract void draw();
   }
   public class Circle extends Shape{
     @Override
     public class void draw() {
       system.out.println("Draw")
     }
   }
   
   public interface Color{
     void fillColor();
   }
   public class GreenColor implements Color {
     @Override
     public void fillColor() {
       system.out.printLn("fillColor")
     }
   }

   public abstract class Shape {
     protected Color color;
     public Shape(Color color) {
       this.color=color;
     }
     public abstract void draw();
   }
   public class Circle extends Shape{
     public Circle(Color color) {
       super(color);
     }
   
     @Override
     public void draw() {
       system.out.printLn("color")
       color.fillColor();
     }
   }
   `   
3. Компоновщик (Composite) - Объединяет объекты в древовидные структуры.
   Пример: компоненты (контейнеры + кнопки)
   Позволяет компоновать в одном объекте много других объектов разных типов.
   `
   interface Container {
     void add(Box box);
     void delete(int number)
   }
   interface Box {
     int getNumber();
   }
   class BigContainer implements Container {
     private List<Box> boxes;
     public void add(Box box) {}
     public void delete(int number) {}
     public List<Box> getBoxes(){}
   }
   `
4. Декоратор (Decorator) - Динамически добавляет новую функциональность объекту.
   Пример: java.io (например, BufferedReader оборачивает FileReader).
   Вкратце это когда класс реализует интерфейс, имплементит его метод и добавляет дополнительные методы для реализации логики
   `
   interface Component{
     doIt()
   }
   class ComponentDecorator implements Component {
      @Override
      public void doIt() {}
      
      public void newMethod(){}
   }
   `
5. Фасад (Facade) - Предоставляет упрощенный интерфейс к сложной системе.
   Пример: тоесть это реализован метод в котором находятся множество взаимодействий с дочерними классами их вызов и логика.
   `
   public class Tree {
     private Roots root = new Roots();
     private Branches branches = new Branches();
     private Trunk trunk = new Trunk();
   
     public void grow() {
       roots.growRoots();
       trunk.growTaller();
       trunk.growAround();
       branches.growLimbs(6);
       branches.growLeaves(500);
     }
   `   
6. Приспособленец (Flyweight) - Это объект, представляющий себя как уникальный экземпляр в разных местах программы, но по 
   факту не являющийся таковым. Тоесть в классе есть метод который служит как фабрикой для создания классов. 
   Тоесь обращаяся к методу этого класса и внутри него уже определяется создавать новый экземпляр или использовать существующий.
   `
   class ComponentFactory {
   
     public Component create(Param param1, Param param2) {
      //Тут принимается решение создавать новый класс или использовать текущий
     }
   }
   `
7. Заместитель (Proxy) - Контролирует доступ к объекту, добавляя промежуточную логику (ленивая загрузка, кеширование).
   Пример: Hibernate Proxy, динамические прокси в Spring AOP

## Приведите_примеры_поведенческих_шаблонов

1. Цепочка обязанностей (Chain of responsibility) - Передает запрос по цепочке обработчиков.
   Пример: Фильтры в Servlet API, Spring Security.
2. Интерпретатор (Interpreter) - Решает часто встречающуюся, но подверженную изменениям, задачу.
   Нужен допустим когда необходимо перевести из русского в английский или из римских чисел в обычные.
3. Итератор (Iterator) - Представляет собой объект, позволяющий получить последовательный доступ к элементам объекта-агрегата 
   без использования описаний каждого + __из объектов, входящих в состав агрегации.
4. Наблюдатель (Observer) - Позволяет объектам уведомлять подписчиков об изменениях.
   Пример: EventListener, RxJava.
5. Состояние (State) - Позволяет объекту менять поведение при изменении состояния.
   Пример: Автомат состояний (например, заказ: "новый" → "оплачен" → "доставлен").
6. Стратегия (Strategy) - Инкапсулирует алгоритмы в отдельные классы, делая их взаимозаменяемыми. Можно применять алгоритм налету
   в зависимости от ситуации. Применяется когда необходим выбор алгоритма обработки. (Допустим нужны разные варианты обработки)
   Мы отделяем процедуру выбора алгоритма от его конкретных реализаций.
   `
   public interface DealExecutorStrategy {
     DealType getDealType();
     void execute(Deal deal);
   }
   public class DealExecutorService{
     private final List<DealExecutorStrategy> executers;
     
     public void execute(Deal deal) {
       var executor = executors.stream()
             .filter(executer -> executor.getDealType() == deal.getDealType())
             .findFirst();
       executor.execute(deal);
     }
   }
   `
   Пример: Сортировка (Comparator в Java).
7. Посетитель (Visitor) - Описывает операцию, которая выполняется над объектами других классов. При изменении класса Visitor
    нет необходимости изменять обслуживаемые классы.


## Что_такое_антипаттерн_и_какие_знаю

1. Антипаттерн (anti-pattern) — это распространённый подход к решению класса часто встречающихся проблем, являющийся 
   неэффективным, рискованным или непродуктивным.
2. Большой комок грязи (Big ball of mud): Система с нераспознаваемой структурой.
3. Божественный объект (God object): Концентрация слишком большого количества функций в одной части системы (классе).
