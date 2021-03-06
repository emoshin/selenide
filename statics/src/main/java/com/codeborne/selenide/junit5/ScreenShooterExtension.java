package com.codeborne.selenide.junit5;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Screenshots;
import com.codeborne.selenide.ex.UIAssertionError;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Logger;

import static com.codeborne.selenide.WebDriverRunner.driver;
import static com.codeborne.selenide.ex.ErrorMessages.screenshot;

/**
 * Use this class to automatically take screenshots in case of ANY errors in tests (not only Selenide errors).
 *
 * How to use in Java:
 * <pre>
 * {@code
 *    @ExtendWith({ScreenShooterExtension.class})
 *    public class MyTest {...}
 * }
 * </pre>
 *
 * How to use in Java (with customization):
 * <pre>
 * {@code
 *   public class MyTest {
 *     @RegisterExtension
 *     static ScreenShooterExtension screenshotEmAll = new ScreenShooterExtension(true);
 *     ...
 *   }
 * }
 * </pre>
 *
 * How to use in Kotlin:
 *
 * <pre>
 *   {@code
 *     @ExtendWith(ScreenShooterExtension::class)
 *     public class MyTest {...}
 *   }
 * </pre>
 *
 * How to use in Kotlin (with customization):
 *
 * <pre>
 * {@code
 *   public class MyTest {
 *     companion object {
 *       @JvmField
 *       @RegisterExtension
 *       val screenshotEmAll: ScreenShooterExtension = ScreenShooterExtension(true);
 *     }
 *     ...
 *   }
 * }
 * </pre>
 *
 * @author Aliaksandr Rasolka
 * @since 4.12.2
 */
public class ScreenShooterExtension implements BeforeAllCallback, AfterEachCallback, AfterAllCallback {
  private static final Logger log = Logger.getLogger(ScreenShooterExtension.class.getName());

  private final boolean captureSuccessfulTests;

  public ScreenShooterExtension() {
    this(false);
  }

  /**
   * @param captureSuccessfulTests param that indicate if need to capture successful tests
   */
  public ScreenShooterExtension(final boolean captureSuccessfulTests) {
    this.captureSuccessfulTests = captureSuccessfulTests;
  }

  /**
   * One-liner to configure Configuration.reportsFolder property.
   *
   * @param folderWithScreenshots Folder to put screenshots to
   *
   * @return current extension instance
   */
  public ScreenShooterExtension to(final String folderWithScreenshots) {
    Configuration.reportsFolder = folderWithScreenshots;
    return this;
  }

  @Override
  public void beforeAll(final ExtensionContext context) {
    final Optional<Class<?>> testClass = context.getTestClass();
    final String className = testClass.isPresent()
      ? testClass.get().getName()
      : "EmptyClass";
    final Optional<Method> testMethod = context.getTestMethod();
    final String methodName = testMethod.isPresent()
      ? testMethod.get().getName()
      : "emptyMethod";
    Screenshots.startContext(className, methodName);
  }

  @Override
  public void afterEach(final ExtensionContext context) {
    if (captureSuccessfulTests) {
      log.info(screenshot(driver()));
    } else {
      context.getExecutionException().ifPresent(error -> {
        if (!(error instanceof UIAssertionError)) {
          log.info(screenshot(driver()));
        }
      });
    }
  }

  @Override
  public void afterAll(final ExtensionContext context) {
    Screenshots.finishContext();
  }
}
