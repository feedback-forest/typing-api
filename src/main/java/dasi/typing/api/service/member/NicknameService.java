package dasi.typing.api.service.member;

import java.text.MessageFormat;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NicknameService {

  private final String[] ADJECTIVES = {
      "사랑스러운", "희망찬", "행복한", "꿈같은", "빛나는", "기쁨넘치는", "여유넘치는", "이쁜미소의",
      "용기가넘치는", "자존감높은", "평화로운", "자유로운", "활기찬", "반짝이는", "신나는", "즐거운",
      "감사한", "응원하는", "환희하는", "환한", "긍정적인", "밝은", "따뜻한", "부드러운", "만족스러운",
      "기운찬", "푸르른", "아름다운", "반가운", "멋진", "찬란한", "환상적인", "대단한", "특별한", "훈훈한",
      "나아가는", "밝아지는", "힘이되는", "새롭게", "충만한", "활짝웃는", "화사한", "단단한", "멀리보는",
      "여유로운", "기대되는", "노력하는", "끈기있는", "인내하는", "실천하는", "해내는", "강인한", "연습하는",
      "열정있는", "흔들리지않는", "무저지지않는", "목표를이루는", "경험하는", "나아지는", "멈추지않는",
      "극복하는", "최선을 다하는", "꾸준한", "성장하는", "발전하는", "도전하는", "성공하는", "배우는",
      "익히는", "변화하는", "혁신적인", "창의적인", "열정적인", "호기심있는", "탐구적인", "도약하는",
      "새로운", "깨닫는", "지혜로운", "스스로", "이끌어가는", "몰입하는", "목표하는", "자신을믿는"
  };

  private final String[] NOUNS = {
      "인턴", "최종합격", "취준생", "성장러", "도전러", "배움러", "취뽀러", "밈마스터", "플렉서", "갓생러",
      "엔잡러", "감성러", "텐션러", "소학행러", "카공러", "인사러", "코덕", "덕후러", "집콕러", "카페투어러",
      "브이로그러", "짠테크러", "오운완러", "릴스러", "챌린저러", "갓생기록러", "덕질러"
  };

  private final Random RANDOM = new Random();


  public String generate() {

    String adjective = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
    String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];

    return MessageFormat.format("{0}{1}", adjective, noun);
  }
}
