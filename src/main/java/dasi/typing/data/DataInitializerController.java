package dasi.typing.data;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/data-initializer")
public class DataInitializerController {

  private final DataInitializerService dataInitializerService;

  @GetMapping
  public void initializeData() {
    dataInitializerService.initializeData();
  }
}
