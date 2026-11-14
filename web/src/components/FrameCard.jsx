export function FrameCard({ children, className = "", innerClassName = "" }) {
  return (
    <div className={`frame-card ${className}`}>
      <span className="frame-crosshair tl" aria-hidden="true" />
      <span className="frame-crosshair tr" aria-hidden="true" />
      <span className="frame-crosshair bl" aria-hidden="true" />
      <span className="frame-crosshair br" aria-hidden="true" />
      <div className={`frame-card-clip ${innerClassName}`}>{children}</div>
    </div>
  );
}
