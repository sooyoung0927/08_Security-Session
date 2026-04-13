package com.wanted.securitysession.domain.user.controller;

import com.wanted.securitysession.domain.user.model.dto.SignupDTO;
import com.wanted.securitysession.domain.user.model.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {


    private final MemberService memberService;

    @GetMapping("/signup")
    public void signup(){
    }

    @PostMapping("/signup")
    public ModelAndView signup(@ModelAttribute SignupDTO signupDTO, ModelAndView mv){

        // @ModelAttribute 사용자의 요청을 받고
        // ModelAndView 처리 결과를 화면으로 출력되도록

        // dml -> 회원가입이 완료된다 -> 데이터 1개 행이 추가되면
        // server애 영향을 받은 행의 수만큼 정수값을 반환해준다
        Integer result = memberService.regist(signupDTO);
        String message = null;
        if(result == null ){
            message ="중복회원이 존재합니다.";
        }else if(result == 0) {
            message="서버에서 오류가 발생하였습니다.";
            mv.setViewName("user/signup");
        }else if(result >= 1){
            message = "회원가입이 완료되었습니다.";
            mv.setViewName("auth/login");
        }

        mv.addObject("message", message);

        return mv;
    }


//    View 에서 Session 에 저장 된 회원의 PK 를 hidden input 같은 걸로 넘기면,
//    사용자가 값을 바꿔서 다른 사람 ID로 글을 등록하는 것처럼 조작할 수 있다.
//    반면 서버에서 현재 로그인한 사용자를 기준으로 작성자를 정하면, 클라이언트가 작성자 정보를 마음대로 바꿀 수 없다.
//    @AuthenticationPrincipal AuthDetails authDetails 를 활용해서 로그인 한 사용자의 정보를 꺼내보자.
//    @PostMapping("/posts")
//    public String createPost(@AuthenticationPrincipal AuthDetails authDetails,
//                             PostRequest request) {
//        postService.create(request.getTitle(), request.getContent(), authDetails.getUsername());
//        return "redirect:/posts";
//    }

}
