var myPlayer;
function checkTime(){
  var cur_time=Math.round((new Date()).getTime() / 1000);
  var cur_pos=cur_time-start_time;
  var player_pos=myPlayer.currentTime();
  if(cur_pos>0 && Math.abs(player_pos-cur_pos)>10){
    myPlayer.currentTime(cur_pos);
  }
  setTimeout("$(function(){checkTime();})",30000);
}
function init(){
  checkTime();
  myPlayer.play();
}
$(function(){
  VideoJS.setup("All");
  myPlayer = $("#video_player").player();//VideoJS.setup("video_player");//$("#video_player").player();
  content_length=$("#content_length").val();
  myPlayer.onError(function(errorText, errorID){
    setTimeout("alert(\"There's something wrong. \" + errorText);",0);
    setTimeout("$(function(){init();})",1000);
  });
  setTimeout("$(function(){init();})",1000);	  
});
