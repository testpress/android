
var iFrames = document.getElementsByTagName("iFrame");
for (i = 0; i < iFrames.length; i++) {
    target = iFrames[i]
    div = document.createElement('div');
    div.className = "videoWrapper";
    div.appendChild(target.cloneNode(true));
    target.parentNode.insertBefore(div, target);
    target.parentNode.removeChild(target);
}
