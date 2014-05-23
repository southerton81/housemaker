package com.kurovsky.houseoftheday.helpactivity;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.kurovsky.houseoftheday.R;
import com.swarmconnect.SwarmActivity;

public class HelpActivity extends SwarmActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.help);
		
		 TextView HelpTextView = (TextView) findViewById(R.id.helpTextView);

		 MovementMethod m = HelpTextView.getMovementMethod();
		 if ((m == null) || !(m instanceof LinkMovementMethod))
			 HelpTextView.setMovementMethod(LinkMovementMethod.getInstance());
		 
		 CharSequence Text = HelpTextView.getText();
		 Text = SetSpanBetweenTokens(Text, "%%", new RelativeSizeSpan(1.3f));
	
		 Text = SetSpanBetweenTokens(Text, "##", new RelativeSizeSpan(1.2f), new UnderlineSpan(), new ClickableSpan()
		 {
			@Override
			public void onClick(View arg0) {
			}
		 });
		 
		 Text = SetSpanBetweenTokens(Text, "##", new RelativeSizeSpan(1.2f), new UnderlineSpan(), new ClickableSpan()
		 {
			@Override
			public void onClick(View arg0) {
			}
		 });
		 
		 Text = SetSpanBetweenTokens(Text, "##", new RelativeSizeSpan(1.2f), new UnderlineSpan(), new ClickableSpan()
		 {
			@Override
			public void onClick(View arg0) {
			}
		 });
		 
		 Text = SetSpanBetweenTokens(Text, "##", new RelativeSizeSpan(1.2f), new UnderlineSpan(), new ClickableSpan()
		 {
			@Override
			public void onClick(View arg0) {
			}
		 });
		 
		 Text = SetSpanBetweenTokens(Text, "##", new RelativeSizeSpan(1.2f), new UnderlineSpan(), new ClickableSpan()
		 {
			@Override
			public void onClick(View arg0) {
			}
		 });
		 
		 Text = SetSpanBetweenTokens(Text, "##", new RelativeSizeSpan(1.2f), new UnderlineSpan(), new ClickableSpan()
		 {
			@Override
			public void onClick(View arg0) {
			}
		 });
		 
		 Text = SetSpanBetweenTokens(Text, "##", new RelativeSizeSpan(1.2f), new UnderlineSpan(), new ClickableSpan()
		 {
			@Override
			public void onClick(View arg0) {
			}
		 });
		 
		 Text = SetSpanBetweenTokens(Text, "##", new RelativeSizeSpan(1.2f), new UnderlineSpan(), new ClickableSpan()
		 {
			@Override
			public void onClick(View arg0) {
			}
		 });
			
		 HelpTextView.setText(Text);
	}
	
	/**
	 * Given either a Spannable String or a regular String and a token, apply
	 * the given CharacterStyle to the span between the tokens, and also remove
	 * tokens.
	 * <p>
	 * For example, {@code setSpanBetweenTokens("Hello ##world##!", "##",
	 * new ForegroundColorSpan(0xFFFF0000));} will return a CharSequence {@code
	 * "Hello world!"} with {@code world} in red.
	 *
	 * @param text The text, with the tokens, to adjust.
	 * @param token The token string; there should be at least two instances of
	 *            token in text.
	 * @param cs The style to apply to the CharSequence. WARNING: You cannot
	 *            send the same two instances of this parameter, otherwise the
	 *            second call will remove the original span.
	 * @return A Spannable CharSequence with the new style applied.
	 *
	 * @see http://developer.android.com/reference/android/text/style/CharacterStyle.html
	 */
	public static CharSequence SetSpanBetweenTokens(CharSequence text,
		String token, CharacterStyle... cs){
		// Start and end refer to the points where the span will apply
		int tokenLen = token.length();
		int start = text.toString().indexOf(token) + tokenLen;
		int end = text.toString().indexOf(token, start);

		if (start > -1 && end > -1)
		{
			// Copy the spannable string to a mutable spannable string
			SpannableStringBuilder ssb = new SpannableStringBuilder(text);
			for (CharacterStyle c : cs)
				ssb.setSpan(c, start, end, 0);

			// Delete the tokens before and after the span
			ssb.delete(end, end + tokenLen);
			ssb.delete(start - tokenLen, start);

			text = ssb;
		}

		return text;
	}
	
}

